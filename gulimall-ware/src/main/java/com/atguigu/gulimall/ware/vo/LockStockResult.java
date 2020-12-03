package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/3 10:57
 * @Version 1.0
 **/
@Data
public class LockStockResult {

    private Long skuId;

    private Integer num;

    private boolean locked;
}
