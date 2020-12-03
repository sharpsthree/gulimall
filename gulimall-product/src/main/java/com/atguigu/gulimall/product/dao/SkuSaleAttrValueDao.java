package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    /**
     *
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据skuId查询sku销售属性组合
     * @param skuId
     * @return
     */
    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
