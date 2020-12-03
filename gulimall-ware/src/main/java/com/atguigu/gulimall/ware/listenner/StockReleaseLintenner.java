package com.atguigu.gulimall.ware.listenner;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
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
 * @Date 2020/5/7 08:07
 * @Version 1.0
 **/
@RabbitListener(queues = {"stock.release.stock.queue"})
@Component
@Slf4j
public class StockReleaseLintenner {


    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     * 下订单成功，库存锁定成功，但是后面的业务调用失败了，导致订单回滚，那么之前锁定的库存就要解锁
     *
     * 只要解锁库存失败，就要告诉服务器解锁失败，消息不能删除
     * @param stockLockedTo
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {

        log.info("******收到解锁库存的信息******");

        try {
            wareSkuService.buidUnlockStock(stockLockedTo);

            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    /**
     * 订单关闭解锁库存
     * @param orderTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {

        log.info("******收到订单关闭，准备解锁库存的信息******");

        try {
            wareSkuService.buidUnlockStock(orderTo);

            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
