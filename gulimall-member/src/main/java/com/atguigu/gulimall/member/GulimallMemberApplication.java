package com.atguigu.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、如何远程调用别的服务
 *  1)、引入openfeign
 *  2)、编写一个接口，告诉springcloud这个接口需要调用远程服务 CouponFeignService
 *      ① 声明接口的每一个方法都是调用哪一个远程服务
 *  3)、开启远程调用功能 @EnableFeignClients
 */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atguigu.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.member.dao")
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
