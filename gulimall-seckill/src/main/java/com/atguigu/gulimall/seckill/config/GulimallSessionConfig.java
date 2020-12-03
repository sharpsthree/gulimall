package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @Description 配置spring session的规则
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/25 08:21
 * @Version 1.0
 **/
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("GULISESSION"); // cookie的名字
        serializer.setDomainName("gulimall.com"); // cookie的作用域
        return serializer;
    }

    /**
     * 存储的序列化机制
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
