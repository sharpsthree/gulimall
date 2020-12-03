/*
package com.atguigu.gulimall.gulimallgateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

*/
/**
 * @Description 网关自定义限流返回
 * @Author 鲁班不会飞
 * @Date 2020/5/13 11:27
 * @Version 1.0
 **//*

@Configuration
public class MySentinelGatewayConfig {

    public MySentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            */
/**
             * 网关限流了请求，就会调用此回调
             * @param serverWebExchange
             * @param throwable
             * @return
             *//*

            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
                String errJson = JSON.toJSONString(error);
                Mono<ServerResponse> responseMono = ServerResponse.ok().body(Mono.just(errJson), String.class);
                return responseMono;
            }
        });
    }
}
*/
