package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/3 10:04
 * @Version 1.0
 **/
@Data
public class WareSkuLockVo {

    private String orderSn;

    private List<OrderItemVo> locks; // 需要锁住的所有库存
}
