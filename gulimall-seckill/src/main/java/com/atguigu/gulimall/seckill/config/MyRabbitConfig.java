package com.atguigu.gulimall.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/4/30 22:57
 * @Version 1.0
 **/
@Slf4j
@Configuration
public class MyRabbitConfig {

    /**
     * 定义RabbitMQ序列化器
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }




}
