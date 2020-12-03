package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存sku的基本信息；pms_sku_info
     * @param skuInfoEntity
     */
    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    /**
     * sku检索分页查询
     * @param params
     * @return
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据spuId查询所有sku信息
     * @param spuId
     * @return
     */
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * 前台查询商品详情页信息
     * @param skuId
     * @return
     */
    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

