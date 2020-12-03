package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description spu规格参数
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/19 22:14
 * @Version 1.0
 **/
@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;
    List<Attr> attrs;
}
