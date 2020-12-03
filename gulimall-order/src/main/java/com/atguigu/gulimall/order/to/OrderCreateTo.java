package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/2 21:16
 * @Version 1.0
 **/
@Data
public class OrderCreateTo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> items;

    private BigDecimal payPrice; // 订单计算的应付价格

    // TODO 暂未实现
    private BigDecimal fare; // 运费

}
