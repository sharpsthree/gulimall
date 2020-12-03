package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/2 16:09
 * @Version 1.0
 **/
@Data
public class SubmitOrderRespVo {

    private OrderEntity orderEntity;

    private Integer code; // 0成功 状态码


}
