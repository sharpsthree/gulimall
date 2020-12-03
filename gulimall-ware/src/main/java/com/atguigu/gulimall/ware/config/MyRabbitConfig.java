package com.atguigu.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

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

//    @RabbitListener(queues = {"stock.release.stock.queue"})
//    public void handle(Message message) {
//
//    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue() {

        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000); // 消息过期时间 2分钟
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }


    /**
     * 交换机与普通队列绑定
     * @return
     */
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    /**
     * 交换机与延迟队列绑定
     * @return
     */
    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

}
