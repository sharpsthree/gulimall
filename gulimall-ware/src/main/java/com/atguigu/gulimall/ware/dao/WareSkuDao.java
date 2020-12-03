package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 08:18:11
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /**
     * 更新库存信息
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    /**
     * 查询sku的库存
     * @param skuId
     * @return
     */
    Long getSkuStock(@Param("skuId") Long skuId);

    /**
     * 根据skuId查询在哪些仓库有该商品
     * @param skuId
     * @return
     */
    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    Integer lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    /**
     * 解锁库存
     * @param skuId 商品skuId
     * @param wareId 仓库id
     * @param count 需要解锁的数量
     */
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);


}
