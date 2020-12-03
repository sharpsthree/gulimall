package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description 封装页面所有可能传递过来的检索字段
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/14 11:18
 * @Version 1.0
 **/
@Data
public class SearchParam {

    private String keyword; // 全文匹配关键字

    private Long catalog3Id; // 三级分类id

    /**
     * saleCount_asc/desc
     * skuPrice_asc/desc
     * hotScore_asc/desc
     */
    private String sort; // 排序

    private Integer hasStock; // 是否只显示有货 0 无库存 1 有库存

    private String skuPrice; // 价格区间

    private List<Long> brandId; // 品牌id

    private List<String> attrs; // 规格属性

    private Integer pageNum = 1; // 页码

    private String _queryString; // 查询字符串

}
