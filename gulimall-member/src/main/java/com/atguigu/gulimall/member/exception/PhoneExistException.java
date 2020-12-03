package com.atguigu.gulimall.member.exception;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/22 11:38
 * @Version 1.0
 **/
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已存在");
    }
}
