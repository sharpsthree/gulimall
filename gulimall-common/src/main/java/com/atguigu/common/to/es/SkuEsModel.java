package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description sku在es保存的数据模型
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/10 11:01
 * @Version 1.0
 **/
@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock; // 是否有库存

    private Long hotScore; // 热度评分

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;


    @Data
    public static class Attrs{

        private Long attrId;

        private String attrName;

        private String attrValue;
    }

}
