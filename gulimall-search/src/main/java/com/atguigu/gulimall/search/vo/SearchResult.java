package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 返回给前台页面显示的结果集封装对象
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/14 23:06
 * @Version 1.0
 **/
@Data
public class SearchResult {


    private List<SkuEsModel> products; // 查询到的商品相关信息


    private Integer pageNum; // 当前页面

    private Long total; // 总记录数

    private Integer totalPages; // 总页码

    private List<Integer> pageNavs; // 用于遍历页码

    private List<BrandVo> brands; // 当前查询到的结果所涉及的所有的品牌

    private List<AttrVo> attrs; // 当前查询到的结果所涉及的所有属性

    private List<CatalogVo> catalogs; // 当前查询到的结果所涉及的所有分类

    private List<NavVo> navs = new ArrayList<>(); //面包屑导航数据

    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo{

        private String navName;

        private String navValue;

        private String link;
    }

    /* ================================= 以上的返回给页面显示的数据 ================================= */

    @Data
    public static class BrandVo{

        private Long brandId;

        private String brandName;

        private String brandImg;
    }

    @Data
    public static class AttrVo{

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{

        private Long catalogId;

        private String catalogName;
    }
}
