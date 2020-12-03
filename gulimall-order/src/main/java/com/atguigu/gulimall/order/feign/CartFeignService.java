package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    /**
     * 获取当前登录用户选中的购物项
     * @return
     */
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> currentUserCartItems();
}
