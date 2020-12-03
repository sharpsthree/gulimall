package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    /**
     * 获取已登录的用户信息
     * @param token 请求地址中携带的令牌
     * @return
     */
    @GetMapping("/userinfo")
    @ResponseBody
    public String userInfo(@RequestParam("token") String token) {
        // 根据请求地址中携带的令牌去redis中查询用户信息
        String str = stringRedisTemplate.opsForValue().get(token);
        return str;
    }

    /**
     * 用户登录时跳转到该方法
     * @param url 请求地址中携带的登录后需要重定向跳转到的url
     * @param model
     * @param sso_token 浏览器上一次登录留下的cookie信息
     * @return
     */
    @GetMapping("login.html")
    public String loginPage(@RequestParam(value = "redirect_url", required = false) String url, Model model,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {

        // 判断浏览器的cookie中是否有上一次登录留下的令牌信息
        if (!StringUtils.isEmpty(sso_token)) { // 令牌信息存在，则直接根据请求地址中的重定向地址直接跳转回去，并将令牌信息以地址参数的方式响应给请求方
            return "redirect:" + url + "?token=" + sso_token;
        }
        // 浏览器cookie中没有存在上一次登录留下的cookie信息，跳转到登录页面，进行登录操作
        // ①第一次登录 ②上一次登录的cookie过期或已删除(退出登录)
        model.addAttribute("url", url);
        return "login";
    }

    /**
     * 处理用户的登录请求
     * @param username
     * @param password
     * @param url
     * @param response
     * @return
     */
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {

            // 1、登录成功用户信息放到缓存
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            // 2、登录成功给浏览器留下一个token信息
            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie); // 给当前服务浏览器设置一个cookie信息
            // 跳回到之前的页面
            return "redirect:" + url + "?token=" + uuid;
        }
        return "login";
    }


}
