package com.atguigu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:18:11
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询采购项
     * @param id 采购单id
     * @return
     */
    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

