package com.atguigu.gulimall.order.vo;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/2 09:12
 * @Version 1.0
 **/

import lombok.Data;

@Data
public class SkuStockVo {

    private Long skuId;
    private Boolean hasStock;
}
