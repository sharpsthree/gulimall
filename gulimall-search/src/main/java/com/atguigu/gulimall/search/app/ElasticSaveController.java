package com.atguigu.gulimall.search.app;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/10 15:22
 * @Version 1.0
 **/
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStstusUp(@RequestBody List<SkuEsModel> esModels) {
        Boolean flag = false;
        try {
             flag = productSaveService.productStatusUp(esModels);
        } catch (Exception e) {
            log.error("ElasticSaveController商品上架产生了错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getCode(), BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getMsg());
        }
        if (!flag) {
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getCode(), BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getMsg());
        }
    }
}
