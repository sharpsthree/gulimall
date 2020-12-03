package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient("gulimall-product")
@Component
public interface ProductFeignService {

    /**
     * 根据skuId查询sku详细信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);
}
