package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/21 04:50
 * @Version 1.0
 **/
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    SmsComponent smsComponent;


    /**
     * 阿里云发送手机架验证码 - 提供给别的服务调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {

        smsComponent.sendCode(phone, code);

        return R.ok();
    }
}
