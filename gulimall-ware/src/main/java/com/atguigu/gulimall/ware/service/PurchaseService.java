package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:18:11
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询未领取的采购单
     * @param params
     * @return
     */
    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    /**
     * 合并采购需求
     * @param mergeVo
     */
    void mergePurchase(MergeVo mergeVo);

    /**
     * 领取采购单
     * @param ids
     */
    void received(List<Long> ids);

    /**
     * 完成采购单
     * @param doneVo
     */
    void done(PurchaseDoneVo doneVo);
}

