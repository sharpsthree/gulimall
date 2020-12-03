package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/12 09:20
 * @Version 1.0
 **/
@Data
public class SecKillOrderTo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer num;

    /**
     * 会员id
     */
    private Long memberId;

}
