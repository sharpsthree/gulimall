package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:15:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    /**
     * 修改订单状态信息
     * @param orderSn 订单id
     * @param payed 订单状态
     */
    void updateOrderStatus(@Param("orderSn") String orderSn, @Param("payed") Integer payed);
}
