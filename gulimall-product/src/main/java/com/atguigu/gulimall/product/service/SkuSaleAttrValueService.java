package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取spu的销售属性组合
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    /**
     * 根据skuId查询sku销售属性组合
     * @param skuId
     * @return
     */
    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

