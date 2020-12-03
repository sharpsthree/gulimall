package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:18:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 将成功采购项的进行入库
     * @param skuId 商品skuid
     * @param wareId 仓库id
     * @param skuNum 采购数量
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询sku是否有库存
     * @param skuIds 要查询的skuId集合
     * @return
     */
    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 锁定某个订单的库存
     * @param wareSkuLockVo
     * @return
     */
    Boolean orderLocKStock(WareSkuLockVo wareSkuLockVo);


    /**
     * 构建解锁库存
     * @param stockLockedTo
     */
    void buidUnlockStock(StockLockedTo stockLockedTo);

    /**
     * 订单关闭解锁库存
     * @param orderTo
     */
    void buidUnlockStock(OrderTo orderTo);
}

