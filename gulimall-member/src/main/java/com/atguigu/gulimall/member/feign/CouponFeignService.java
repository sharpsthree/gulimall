package com.atguigu.gulimall.member.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 1、@FeignClient("gulimall-coupon") 告诉springcloud 这个接口是一个远程客户端
 */
@FeignClient("gulimall-coupon")
@Component
public interface CouponFeignService {

//    @RequestMapping("/api/coupon/coupon/member/list")
    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupons();
}
