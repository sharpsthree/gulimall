package com.atguigu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:18:11
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据订单号查询库存工作单信息
     * @param orderSn
     * @return
     */
    WareOrderTaskEntity getByOrderSn(String orderSn);
}

