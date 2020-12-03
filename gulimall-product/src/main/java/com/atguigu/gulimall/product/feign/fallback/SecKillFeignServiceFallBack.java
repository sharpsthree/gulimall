package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SecKillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/13 02:21
 * @Version 1.0
 **/
@Slf4j
@Component
public class SecKillFeignServiceFallBack implements SecKillFeignService {

    @Override
    public R getSkuSecKillInfo(Long skuId) {
        log.info("熔断方法调用。。。getSkuSecKillInfo");
        return R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
    }
}
