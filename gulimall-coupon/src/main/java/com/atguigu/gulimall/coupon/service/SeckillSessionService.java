package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 02:05:34
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取最近三天秒杀的活动场次
     * @return
     */
    List<SeckillSessionEntity> getLatest3DaySession();
}

