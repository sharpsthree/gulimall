package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/1 15:31
 * @Version 1.0
 **/
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 根据会员id查询收货地址
     * @param memberId
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddresses(@PathVariable("memberId") Long memberId);

}
