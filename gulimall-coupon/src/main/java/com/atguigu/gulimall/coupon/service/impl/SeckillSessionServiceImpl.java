package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取最近三天秒杀的活动场次
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {


        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long sessionId = session.getId();
                List<SeckillSkuRelationEntity> relationEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionId));
                session.setRelationSkus(relationEntityList);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建开始时间
     * @return
     */
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;

        LocalDateTime start = LocalDateTime.of(now, min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return format;
    }

    /**
     * 构建结束时间
     * @return
     */
    private String getEndTime() {
        LocalDate now = LocalDate.now(); // 获取当前时间
        LocalDate finalDate = now.plusDays(2); // 加两天
        LocalTime max = LocalTime.MAX;

        LocalDateTime start = LocalDateTime.of(finalDate, max);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return format;
    }
}