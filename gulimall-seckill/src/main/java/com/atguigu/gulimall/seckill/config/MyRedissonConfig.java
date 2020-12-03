package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/13 11:12
 * @Version 1.0
 **/
@Configuration
public class MyRedissonConfig {

    /**
     * 对Redisson的使用都是通过RedissonClient
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        // 单Redis节点模式
        // 1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 2、根据config创建RedissonClient示例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
