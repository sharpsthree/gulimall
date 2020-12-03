package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/11 12:20
 * @Version 1.0
 **/
@Data
public class SeckillSessionsWithSkus {
    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuVo> relationSkus;
}
