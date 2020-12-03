package com.atguigu.gulimall.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.net.UnknownHostException;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/12 21:42
 * @Version 1.0
 **/
@Configuration
public class MyRedisConfig {

    // 修改Redis默认使用的java的序列化器
    // 手动添加一个序列化器放入容器中

    //@Bean("redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 修改默认的序列化方式
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
