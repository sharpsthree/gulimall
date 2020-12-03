package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    /**
     * 锁定某个订单的库存
     * @param wareSkuLockVo
     * @return
     */
    @PostMapping("/ware/waresku/lock/order")
    R orderLocKStock(@RequestBody WareSkuLockVo wareSkuLockVo);
}
