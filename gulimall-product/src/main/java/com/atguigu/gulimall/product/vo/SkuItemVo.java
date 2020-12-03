package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/19 15:42
 * @Version 1.0
 **/
@Data
public class SkuItemVo {

    SkuInfoEntity info; // sku基本信息

    boolean hasStock = true; // 是否有库存

    List<SkuImagesEntity> images; // sku图片信息

    List<SkuItemSaleAttrVo> saleAttrs;  // spu的销售属性组合信息

    SpuInfoDescEntity desp; // spu介绍信息

    List<SpuItemAttrGroupVo> attrGroups; // spu规格参数

    SecKillInfoVo secKillInfoVo; // 商品秒杀信息
}
