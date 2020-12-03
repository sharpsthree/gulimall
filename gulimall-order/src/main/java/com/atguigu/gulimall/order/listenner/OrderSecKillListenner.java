package com.atguigu.gulimall.order.listenner;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/12 09:49
 * @Version 1.0
 **/
@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSecKillListenner {

    @Autowired
    OrderService orderService;

    /**
     * 监听队列，创建秒杀商品订单
     * @param secKillOrderTo
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler
    public void listenner(SecKillOrderTo secKillOrderTo, Channel channel, Message message) throws IOException {


        try {
            log.info("收到秒杀的商品信息，订单号：{}",secKillOrderTo.getOrderSn());
            orderService.createSecKillOrder(secKillOrderTo);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

}
