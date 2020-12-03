package com.atguigu.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/20 10:24
 * @Version 1.0
 **/
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
