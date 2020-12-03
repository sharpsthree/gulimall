package com.atguigu.common.constant.pms;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/6 12:10
 * @Version 1.0
 **/
public class ProductConstant {


    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),ARRT_TYPE_SALE(0, "销售属性");

        private int code;
        private String msg;

        AttrEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum SpuStatusEnum {
        NEW_SPU(0, "新建"),UP_SPU(1, "上架"), DOWN_SPU(1, "下架");

        private int code;
        private String msg;

        SpuStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum CacheEnum{

        CATALOG_JSON_KEY("catalogJson");

        private String key;

        CacheEnum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

}
