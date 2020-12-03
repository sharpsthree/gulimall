package com.atguigu.gulimall.member.config;

import com.atguigu.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/8 17:50
 * @Version 1.0
 **/
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor interceptor;

    /**
     * 添加自定义的拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
