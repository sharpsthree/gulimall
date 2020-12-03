package com.atguigu.gulimall.member.exception;

/**
 * @Description TODO
 * @Author 鲁班爱喝旺仔
 * @Date 2020/4/22 11:38
 * @Version 1.0
 **/
public class UserNameExistException extends RuntimeException {

    public UserNameExistException() {
        super("用户名已存在");
    }
}
