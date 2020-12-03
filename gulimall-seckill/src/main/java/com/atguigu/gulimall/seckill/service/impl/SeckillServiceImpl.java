package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedsTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/10 23:37
 * @Version 1.0
 **/
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码


    /* ===============================================================  定时任务上架秒杀商品 start ================================================================ */

    /**
     * 每天晚上3点，上架最近三天需要参与秒杀的商品
     */
    @Override
    public void uploadSeckillSkuLatetst3Days() {
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessionData = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            // 缓存到Rediss
            // 1、缓存活动信息
            saveSessionInfos(sessionData);
            // 2、缓存商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    /**
     * 缓存活动信息
     *
     * @param sessionData
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionData) {
        if (sessionData != null) {
            sessionData.stream().forEach(session -> {
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();

                String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

                // 判断缓存中是否已经存在本次上架的信息了
                Boolean hasKey = stringRedisTemplate.hasKey(key);

                if (!hasKey) {
                    List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());

                    stringRedisTemplate.opsForList().leftPushAll(key, collect);
                }
            });
        }

    }

    /**
     * 缓存商品信息
     *
     * @param sessionData
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionData) {

        if (sessionData != null) {
            sessionData.stream().forEach(session -> {
                BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

                session.getRelationSkus().stream().forEach(seckillSkuVo -> {

                    if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {
                        SecKillSkuRedsTo secKillSkuRedsTo = new SecKillSkuRedsTo();
                        // 1、sku基本信息
                        R skuInfo = productFeignService.skuInfo(seckillSkuVo.getSkuId());
                        if (skuInfo.getCode() == 0) {
                            SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            secKillSkuRedsTo.setSkuInfoVo(skuInfoVo);
                        }

                        // 2、sku秒杀信息
                        BeanUtils.copyProperties(seckillSkuVo, secKillSkuRedsTo);

                        // 3、设置当前商品的秒杀时间信息
                        secKillSkuRedsTo.setStartTime(session.getStartTime().getTime());
                        secKillSkuRedsTo.setEndTime(session.getEndTime().getTime());

                        // 4、设置商品的随机码
                        String token = UUID.randomUUID().toString().replace("-", "");
                        secKillSkuRedsTo.setRandomCode(token);

                        // 缓存到redis
                        String json = JSON.toJSONString(secKillSkuRedsTo);
                        ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), json);

                        // 5、使用库存作为分布式信号量 限流
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        BigDecimal seckillCount = seckillSkuVo.getSeckillCount();
                        int count = Integer.parseInt(seckillCount.toString());
                        // 设置信号量，商品可以秒杀的件数昨为信号量
                        semaphore.trySetPermits(count);
                    }

                });
            });
        }


    }
    /* ===============================================================  定时任务上架秒杀商品 end ================================================================ */

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public List<SecKillSkuRedsTo> getCurrentSecKillSkusBlockHandler(BlockException e){
        log.error("getCurrentSecKillSkus被限流了");
        return null;
    }

    /**
     * 返回当前时间可以参与的秒杀的商品信息
     *
     * @return
     */
    @SentinelResource(value = "getCurrentSecKillSkusResource", blockHandler = "getCurrentSecKillSkusBlockHandler")
    @Override
    public List<SecKillSkuRedsTo> getCurrentSecKillSkus() {

        // 1、确定当前时间属于哪个秒杀场次
        long nowTime = new Date().getTime();

        // 1.5.0 版本开始可以利用 try-with-resources 特性（使用有限制）
        // 资源名可使用任意有业务语义的字符串，比如方法名、接口名或其它可唯一标识的字符串。
        try (Entry entry = SphU.entry("secKillSkus")) {
            // 被保护的业务逻辑

            // 得到缓存中所有以seckill:sessions:开头的信息
            Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys) {
                // key => "seckill:sessions:1589248800000_1589263200000"
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] str = replace.split("_");

                Long startTime = Long.parseLong(str[0]);
                Long endTime = Long.parseLong(str[1]);

                if (nowTime >= startTime && nowTime <= endTime) {

                    List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);

                    BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

                    List<String> list = hashOps.multiGet(range);

                    if (list != null && list.size() > 0) {

                        List<SecKillSkuRedsTo> collect = list.stream().map(item -> {
                            SecKillSkuRedsTo secKillSkuRedsTo = JSON.parseObject(item, SecKillSkuRedsTo.class);
                            return secKillSkuRedsTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }

                    break;
                }
            }
        } catch (BlockException e) {
            // 资源访问阻止，被限流或被降级
            // 在此处进行相应的处理操作
            log.error("资源被限流，{}",e.getMessage());
        }

        return null;
    }

    /**
     * 查询某个sku的秒杀信息 - 商品详情 预告秒杀信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SecKillSkuRedsTo getSkuSecKillInfo(Long skuId) {
        // 1、找到所有需要参与秒杀的的商品的key
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();

        if (keys != null && keys.size() > 0) {

            String regx = "\\d_" + skuId;

            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SecKillSkuRedsTo secKillSkuRedsTo = JSON.parseObject(json, SecKillSkuRedsTo.class);

                    // 处理随机码
                    long currTime = new Date().getTime();

                    if (currTime >= secKillSkuRedsTo.getStartTime() && currTime <= secKillSkuRedsTo.getEndTime()) {

                    } else {
                        // 不是秒杀时间，把随机码置空
                        secKillSkuRedsTo.setRandomCode(null);
                    }

                    return secKillSkuRedsTo;
                }
            }
        }

        return null;
    }

    /**
     * 秒杀商品
     *
     * @param killId 场次id_商品skuid
     * @param key    秒杀需要携带的令牌
     * @param num    秒杀的数量
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        BoundHashOperations<String, String, String> hasOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        // 1、校验数据
        String jsonStr = hasOps.get(killId);
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        } else {
            SecKillSkuRedsTo secKillSkuRedsTo = JSON.parseObject(jsonStr, SecKillSkuRedsTo.class);
            Long startTime = secKillSkuRedsTo.getStartTime();
            Long endTime = secKillSkuRedsTo.getEndTime();
            long currTime = new Date().getTime();
            // 校验秒杀时间合法性
            if (currTime >= startTime && currTime <= endTime) {
                // 校验随机码和商品skuId是否一致 并且场次是当前秒杀的场次
                String randomCode = secKillSkuRedsTo.getRandomCode();
                String redis_killId = secKillSkuRedsTo.getPromotionSessionId() + "_" + secKillSkuRedsTo.getSkuId();
                if (killId.equals(redis_killId) && key.equals(randomCode)) {
                    // 校验购物数量是否合理
                    BigDecimal seckillLimit = secKillSkuRedsTo.getSeckillLimit();
                    BigDecimal killCount = new BigDecimal(num + "");
                    // 判断本次购物的数量，是否小于每人限购的数量
                    if (killCount.compareTo(seckillLimit) == -1 || killCount.compareTo(seckillLimit) == 0) {
                        // 校验当前用户是否已经购买过该商品了
                        // 只要秒杀成功在redis缓存一个该用户的购买信息 key -> userId_PromotionSessionId_skuId
                        String rediKey = memberRespVo.getId() + "_" + secKillSkuRedsTo.getPromotionSessionId() + "_" + secKillSkuRedsTo.getSkuId();
                        // 过期时间
                        long ttlTime = endTime - startTime;
                        // setIfAbsent() 只有key不存在才会占位成功
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(rediKey, num.toString(), ttlTime, TimeUnit.MILLISECONDS);
                        if (aBoolean) { // 占位成功，当前用户没有购买过 //TODO 解决：假设每个用户限购2个，但是第一次用户只购买了一件商品
                            // 校验当前商品的信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);

                            // 校验能不能拿到信号量
                            boolean tryAcquire = semaphore.tryAcquire(num);
                            if (tryAcquire) {
                                // 秒杀成功，快速下单 发送一个MQ消息
                                // 构建消息数据
                                String orderSn = IdWorker.getTimeId();

                                SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                                secKillOrderTo.setOrderSn(orderSn);
                                secKillOrderTo.setMemberId(memberRespVo.getId());
                                secKillOrderTo.setNum(num);
                                secKillOrderTo.setPromotionSessionId(secKillSkuRedsTo.getPromotionSessionId());
                                secKillOrderTo.setSkuId(secKillSkuRedsTo.getSkuId());
                                secKillOrderTo.setSeckillPrice(secKillSkuRedsTo.getSeckillPrice());
                                // 发送消息
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", secKillOrderTo);

                                return orderSn;
                            }

                        }
                    }
                }
            }
        }

        return null;
    }
}
