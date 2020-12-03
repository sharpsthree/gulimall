package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {

    /**
     * 上传到es
     * @param esModels
     * @return true 有错误 false 无错误
     * @throws IOException
     */
    Boolean productStatusUp(List<SkuEsModel> esModels) throws IOException;
}
