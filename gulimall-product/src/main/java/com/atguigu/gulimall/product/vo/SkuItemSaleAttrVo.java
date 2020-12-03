package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description spu的销售属性组合信息
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/19 22:20
 * @Version 1.0
 **/
@Data
@ToString
public class SkuItemSaleAttrVo {
    private Long attrId;

    private String attrName;

    private List<AttrValWithSkuIdVo> attrValues;
}
