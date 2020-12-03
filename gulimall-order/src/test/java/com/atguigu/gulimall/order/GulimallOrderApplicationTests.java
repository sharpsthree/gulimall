package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

	@Autowired
	AmqpAdmin amqpAdmin;

	@Autowired
	RabbitTemplate rabbitTemplate;


	/**
	 * 1、如果创建Exchange、Queue、Binding
	 * 	① 使用AmqpAdmin进行创建
	 * 2、如何收发消息
	 */
	@Test
	public void creatExchange() {

		Exchange exchange = new DirectExchange("hello-java-exchange", true, false);
		amqpAdmin.declareExchange(exchange);

		log.info("Exchange[{}]创建成功！", "hello-java-exchange");
	}

	/**
	 * 创建队列
	 */
	@Test
	public void createQueue() {

		Queue queue = new Queue("hello-java-queue", true, false, false);
		amqpAdmin.declareQueue(queue);

		log.info("Queue[{}]创建成功！", "hello-java-queue");

	}

	/**
	 * 创建Binding
	 */
	@Test
	public void createBinding() {

		// Binding(String destination,  【目的地（交换机或者Queue)】
		// DestinationType destinationType, 【类型 （Exhange、Queue）】
		// String exchange, 【交换机】
		// String routingKey, 【路由键】
		// Map<String, Object> arguments)
		Binding binding = new Binding("hello-java-queue",
				Binding.DestinationType.QUEUE,
				"hello-java-exchange",
				"hello.java",
				null);
		amqpAdmin.declareBinding(binding);

		log.info("Binding[{}]创建成功！", "binding");
	}


	/**
	 * 发送消息
	 * 如果发送的消息是个对象，会使用序列化机制，将对象写出去，所以对象必须实现Serializable
	 */
	@Test
	public void sendMessage() {
		for (int i = 0; i < 10; i++) {
			OrderEntity orderEntity = new OrderEntity();
			orderEntity.setMemberId(1111L);
			orderEntity.setMemberUsername("鲁班不会飞" + i);
			rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
			log.info("消息发送成功！");
		}

	}


}
