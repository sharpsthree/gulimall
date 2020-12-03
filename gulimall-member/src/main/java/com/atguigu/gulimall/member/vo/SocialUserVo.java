package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/24 18:56
 * @Version 1.0
 **/
@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
