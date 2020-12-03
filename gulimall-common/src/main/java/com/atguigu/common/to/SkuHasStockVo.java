package com.atguigu.common.to;

import lombok.Data;

/**
 * @Description 商品服务远程调用返回的结果集对象
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/10 13:38
 * @Version 1.0
 **/
@Data
public class SkuHasStockVo {

    private Long skuId;
    private Boolean hasStock;
}
