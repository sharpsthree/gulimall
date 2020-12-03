package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/1 18:34
 * @Version 1.0
 **/
@Configuration
public class GuliFeignConfig {

    /**
     * 解决fein远程调用丢失请求头
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1、RequestContextHolder 拿到当前的请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    // 原始请求 页面发起的老请求
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        // 获取原始请求的头数据 cookie
                        String cookie = request.getHeader("Cookie");

                        // 给feign生成的心请求设置请求头cookie
                        template.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
