package com.atguigu.common.to.mq;

import lombok.Data;


/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/6 15:53
 * @Version 1.0
 **/
@Data
public class StockLockedTo {

    private Long id; // 库存工作单id

    private StockDetailTo stockDetail; // 工作单详情id
}
