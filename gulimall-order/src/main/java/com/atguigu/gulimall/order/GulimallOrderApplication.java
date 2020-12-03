package com.atguigu.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景 RabbitAutoConfiguration 自动生效
 * 2、自动配置了 RabbitConnectionFactoryBean、RabbitTemplate、AmqpAdmin、RabbitMessagingTemplate
 */

/**
 * 如果发现事务加不上。开启基于注解的事务功能  @EnableTransactionManagement
 *     如果要真的开启什么功能就显式的加上这个注解。。。。
 *
 * 事务的最终解决方案；
 *       1）、普通加事务。导入jdbc-starter，@EnableTransactionManagement，加@Transactional
 *       2）、方法自己调自己类里面的加不上事务。
 *             1）、导入aop包，开启代理对象的相关功能
 *                  <dependency>
 *                     <groupId>org.springframework.boot</groupId>
 *                     <artifactId>spring-boot-starter-aop</artifactId>
 *                  </dependency>
 *             2）、获取到当前类真正的代理对象，去掉方法即可
 *                    1）、@EnableAspectJAutoProxy(exposeProxy = true):暴露代理对象 开启aspectj动态代理
 *                    2）、获取代理对象；
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@MapperScan("com.atguigu.gulimall.order.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallOrderApplication.class, args);
	}

}
