package com.atguigu.gulimall.seckill.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
@Component
public interface CouponFeignService {

    /**
     * 获取最近三天秒杀的活动场次
     * @return
     */
    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
