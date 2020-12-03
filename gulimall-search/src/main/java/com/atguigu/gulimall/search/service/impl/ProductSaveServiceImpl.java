package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 想es索引数据
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/10 15:29
 * @Version 1.0
 **/
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 上传到es
     * @param esModels
     * @return true 有错误 false 无错误
     * @throws IOException
     */
    @Override
    public Boolean productStatusUp(List<SkuEsModel> esModels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel esModel : esModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(esModel.getSkuId().toString());
            String json = JSON.toJSONString(esModel);
            indexRequest.source(json, XContentType.JSON);
            bulkRequest.add(indexRequest);

        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // TODO 处理产生的错误
        boolean hasFaile = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("ProductSaveServiceImpl.productStstusUp 上架成功商品：{} ,返回的数据：{}",collect,bulk.toString());

        return hasFaile;

    }
}
