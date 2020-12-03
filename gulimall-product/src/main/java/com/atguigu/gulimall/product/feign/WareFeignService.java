package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description 远程调用库存服务
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/10 14:26
 * @Version 1.0
 **/
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
