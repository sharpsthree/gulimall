package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/11 12:54
 * @Version 1.0
 **/
@Data
public class SkuInfoVo {


    private Long skuId;
    /**
     * spuId
     */
    private Long spuId;
    /**
     * sku名称
     */
    private String skuName;
    /**
     * sku介绍描述
     */
    private String skuDesc;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 默认图片
     */
    private String skuDefaultImg;
    /**
     * 标题
     */
    private String skuTitle;
    /**
     * 副标题
     */
    private String skuSubtitle;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 销量
     */
    private Long saleCount;
}
