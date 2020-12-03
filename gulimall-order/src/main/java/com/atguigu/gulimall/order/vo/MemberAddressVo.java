package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 会员收货地址列表
 * @Author 鲁班不会飞
 * @Date 2020/5/1 15:10
 * @Version 1.0
 **/
@Data
public class MemberAddressVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     * 收货人姓名
     */
    private String name;
    /**
     * 电话
     */
    private String phone;
    /**
     * 邮政编码
     */
    private String postCode;
    /**
     * 省份/直辖市
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区
     */
    private String region;
    /**
     * 详细地址(街道)
     */
    private String detailAddress;
    /**
     * 省市区代码
     */
    private String areacode;
    /**
     * 是否默认
     */
    private Integer defaultStatus;
}
