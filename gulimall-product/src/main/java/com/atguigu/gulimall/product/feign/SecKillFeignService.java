package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SecKillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/11 19:48
 * @Version 1.0
 **/
@FeignClient(value = "gulimall-seckill", fallback = SecKillFeignServiceFallBack.class)
@Component
public interface SecKillFeignService {

    /**
     * 查询某个sku的秒杀信息
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}
