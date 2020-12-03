package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/6 20:09
 * @Version 1.0
 **/
@Data
public class StockDetailTo {

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;

    /**
     * 库存锁定状态（1：已锁定 2：已解锁 3：扣减了库存）
     */
    private Integer lockStatus;
}
