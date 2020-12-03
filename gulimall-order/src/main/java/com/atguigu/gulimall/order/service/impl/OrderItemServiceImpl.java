package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@RabbitListener(queues = {"hello-java-queue"})
@Slf4j
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * queues 声明需要监听的Queue
     * @param message 监听得到的消息 头+体
     * @param orderEntity 返回的数据 spring会自动类型转换
     * @param channel 通道
     */
    @RabbitHandler
    public void recieveMessage(Message message, OrderEntity orderEntity, Channel channel) {

        log.info("接受到了消息,[{}],类型[{}]", orderEntity, message.getClass());

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //  # 手动ack消息，不使用默认的消费端确认
        //spring.rabbitmq.listener.simple.acknowledge-mode=manual
        // 手动模式下，签收消息 参数二：是否批量签收
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}