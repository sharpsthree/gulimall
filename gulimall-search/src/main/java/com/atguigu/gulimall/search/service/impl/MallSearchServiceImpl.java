package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/14 11:22
 * @Version 1.0
 **/
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    /**
     * 去es检索需要的信息
     * @param param 检索的所有参数
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = null;

        // 1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 2、执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            // 3、分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 1、准备检索请求
     * # 模糊匹配 过滤（属性、分类、品牌、价格区间、库存）排序 分页 高亮 聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /*  模糊匹配 过滤（属性、分类、品牌、价格区间、库存） */
        // 1、构建 bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1、must - query
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        /* ========================================= 1.2、构建 bool - filter (s)  ========================================= */
        // ① 构建分类过滤 catalogId - filter
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // ② 构建品牌过滤 brandId - filter
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // ③ 构建属性过滤 attrs - filter
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attrStr : param.getAttrs()) {
                // attrs=1_5寸:8寸&attrs=2_8G:16G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] str = attrStr.split("_");
                String attrId = str[0]; // 检索的属性id
                String[] attrValue = str[1].split(":"); // 检索的属性的值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None); // ScoreMode.None 不参与评分

                boolQuery.filter(nestedQuery);
            }

        }

        // ④ 构建库存过滤 hasStock - filter
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // ⑤ 构建价格过滤 skuPrice - filter
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            // skuPrice -> 1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] str = param.getSkuPrice().split("_");
            if (str.length == 2) { // skuPrice -> 1_500
                rangeQuery.gte(str[0]).lte(str[1]);
            } else if (str.length == 1) {
                if (param.getSkuPrice().startsWith("_")) { // skuPrice -> _500
                    rangeQuery.lte(str[0]);
                }
                if (param.getSkuPrice().endsWith("_")) { // skuPrice -> 500_
                    rangeQuery.gte(str[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        /* ========================================= 1.2、构建 bool - filter (e)  ========================================= */
        sourceBuilder.query(boolQuery);


        /*  排序 分页 高亮  */
        // 2.1、排序 sort
        //  saleCount_asc/desc  skuPrice_asc/desc hotScore_asc/desc
        if (!StringUtils.isEmpty(param.getSort())) {
            String sortStr = param.getSort();
            String[] strs = sortStr.split("_");
            SortOrder order = strs[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(strs[0], order);
        }
        // 2.2、分页
        if (param.getPageNum() != null) {
            sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE); // from = (pageNum - 1) * pageSize
            sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        }
        // 2.3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        /* ========================================= 3、构建聚合分析 (s)  ========================================= */

        // ① brand_agg 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg"); // 聚合名
        brand_agg.field("brandId").size(50); // 聚合的字段名 和多少个
        // brand_agg 品牌聚合里的子聚合 brand_name_agg 品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        // brand_agg 品牌聚合里的子聚合 brand_img_agg 品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        sourceBuilder.aggregation(brand_agg);

        // ② catalog_agg 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        // catalog_agg 分类聚合里的子聚合 catalog_name_agg 分类名字聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        sourceBuilder.aggregation(catalog_agg);

        // ③ attr_agg 属性的聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // attr_id_agg 属性id聚合 聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // attr_name_agg 属性名聚合 ---> attr_id_agg 属性id聚合里的子聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // attr_value_agg 属性值聚合 ---> attr_id_agg 属性id聚合里的子聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);

        sourceBuilder.aggregation(attr_agg);
        /* ========================================= 3、构建聚合分析 (e)  ========================================= */


        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL" + s);
        return searchRequest;
    }

    /**
     * 3、分析响应数据，封装成我们需要的格式
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        // 1、封装商品信息
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) { // 如果带有检索字段  则获取高亮显示的skuTitle
                    HighlightField skuTitleHighlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = skuTitleHighlightField.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                esModels.add(skuEsModel);
            }
        }

        result.setProducts(esModels);

        /* ========================================= 2、封装聚合信息 (s)  ========================================= */
        // ① 品牌聚合数据
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brand_buckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brand_buckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            // 得到品牌聚合的子聚合 品牌图片聚合
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            // 得到品牌聚合的子聚合 品牌名字聚合
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandImg(brandImg);
            brandVo.setBrandName(brandName);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // ② 分类聚合数据
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

            String keyAsString = bucket.getKeyAsString();

            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            // 获取分类名子聚合 catalog_name_agg
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // ③ 属性聚合信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 得到属性的名称
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {

                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);

        }

        result.setAttrs(attrVos);

        /* ========================================= 2、封装聚合信息 (e)  ========================================= */



        result.setPageNum(param.getPageNum());

        long total = hits.getTotalHits().value;
        result.setTotal(total);

        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        // 构建遍历需要的页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        // 构建面包屑属性导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(item -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = item.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.info(Long.parseLong(s[0]));

                // 封装当前请求参数中已经选择的属性的id - 为了前台显示的时候排除显示这些id
                result.getAttrIds().add(Long.parseLong(s[0]));

                if (r.getCode() == 0) {
                    AttrResponseVo responseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(responseVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                // 取消面包屑以后，重新设置面包屑需要显示的url地址
                String replace = replaceQueryString(param, item, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(navVos);
        }

        // 构建品牌和分类的面包屑导航
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();

            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = null;
                for (BrandVo brand : brands) {
                    buffer.append(brand.getName()+";");

                    replace = replaceQueryString(param, brand.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());

                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

            }
            navs.add(navVo);

        }


        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
