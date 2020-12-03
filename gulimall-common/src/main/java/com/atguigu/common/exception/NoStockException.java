package com.atguigu.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/3 11:33
 * @Version 1.0
 **/
public class NoStockException extends RuntimeException {

    @Getter @Setter
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id："+ skuId + "库存不足！");
    }

    public NoStockException(String msg) {
        super(msg);
    }


}
