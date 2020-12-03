package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.OssComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description oss对象存储
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/5 02:38
 * @Version 1.0
 **/
@RestController
@RequestMapping("thirdparty")
public class OssController {

    @Autowired
    private OssComponent ossComponent;

    @GetMapping(value = "/oss/policy")
    public R policy() {
        Map<String, String> policy = ossComponent.policy();

        return R.ok().put("data",policy);
    }

}
