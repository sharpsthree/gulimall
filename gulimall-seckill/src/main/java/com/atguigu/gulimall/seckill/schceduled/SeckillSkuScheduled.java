package com.atguigu.gulimall.seckill.schceduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Description 秒杀商品定时上架
 * @Author 鲁班不会飞
 * @Date 2020/5/10 23:28
 * @Version 1.0
 **/
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill_upload:lock";

    /**
     * 每天晚上3点，上架最近三天需要参与秒死的商品
     * 测试：0/5 * * * * ?
     * 三点：0 0 3 * * ?
     */
    @Scheduled(cron = "0 0 3 * * ? ")
    public void uploadSeckillSkuLatetst3Days() {
        // 1、重复上架无需处理
        log.info("******上架秒杀商品信息******");

        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS); // 加锁

        try {
            seckillService.uploadSeckillSkuLatetst3Days();
        }finally {
            lock.unlock(); // 解锁
        }
    }
}
