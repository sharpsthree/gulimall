package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/1 15:21
 * @Version 1.0
 **/
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;



}
