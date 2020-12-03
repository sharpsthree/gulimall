package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUserVo;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth/login")
    public R oauthLogin(@RequestBody SocialUserVo socialUserVo) throws Exception;
}
