package com.atguigu.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、如何使用nacos作为配置中心统一管理配置
 *      1）、引入依赖
 *      2）、创建一个 bootstrap.properties
 *      3）、需要给配置中心默认添加一个 数据集（Data Id）  数据集默认名: 当前应用名.properties （gulimall-coupon.properties）
 *      4）、在gulimall-coupon.properties里添加修改配置
 *      5)、动态获取配置中心的配置的内容  添加注解
 *          @RefreshScope
 *      6)、如果配置中心和当前应用配置文件同时配置了相同的内容，则优先使用配置中心配置的内容
 */

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.coupon.dao")
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
