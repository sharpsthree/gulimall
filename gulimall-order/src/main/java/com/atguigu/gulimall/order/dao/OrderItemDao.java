package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:15:16
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
