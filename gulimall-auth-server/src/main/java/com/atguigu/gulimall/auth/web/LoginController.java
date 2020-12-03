package com.atguigu.gulimall.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ums.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/20 14:32
 * @Version 1.0
 **/
@Slf4j
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送验证码 - 调用第三方服务向阿里云发送验证码
     * @param phone
     * @return
     */
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            // 获取redis这个key存储的时间
            String[] str = redisCode.split("_");
            long createCacheTime = Long.parseLong(str[1]);
            // 判断的是不是60秒内
            if (System.currentTimeMillis() - createCacheTime < 60000) { // 由于存储的时候的毫秒 需要*1000
                return R.error(BizCodeEnume.VAILD_SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.VAILD_SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);
        // 手机号号和验证码存入redis  5分钟过期
        String redisStr = code + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redisStr,5, TimeUnit.MINUTES);


        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     * 用户注册
     * RedirectAttributes 模拟重定向携带数据
     * 原理:利用session，将数据存放在session中，重要跳到下一个页面取出这个数据后，session里面的数据就会删掉
     * 1、需要解决分布式session问题
     * @param registVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) { // 校验错误 收集错误信息
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 校验验证码
        String code = registVo.getCode();
        String str = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
        if (!StringUtils.isEmpty(str)) { // 判断验证是否过期
            String redisCode = str.split("_")[0];
            if (code.equalsIgnoreCase(redisCode)) { // 验证码正确
                // 删掉redis中存储的验证码；令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registVo.getPhone());
                // 调用远程服务注册
                R r = memberFeignService.regist(registVo);
                if (r.getCode() == 0) { // 成功
                    return "redirect:http://auth.gulimall.com/login.html";

                } else { // 注册失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码超时");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }


    /**
     * 会员登录
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes,
                        HttpSession session) {

        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            log.info("普通用户登录成功，用户信息：{}", memberRespVo);
            session.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
            return "redirect:http://gulimall.com";
        } else {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }


    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {

        Object loginUser = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }

    }


}
