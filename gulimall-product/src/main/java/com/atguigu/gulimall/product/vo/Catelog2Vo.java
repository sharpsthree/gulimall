package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 前台显示的二级分类vo
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/11 21:40
 * @Version 1.0
 **/

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {

    private String catalog1Id;

    private List<Catelog3Vo> catalog3List;

    private String id;

    private String name;


    /**
     * 内部封装的三级分类vo
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{

        private String catalog2Id;

        private String id;

        private String name;

    }


}
