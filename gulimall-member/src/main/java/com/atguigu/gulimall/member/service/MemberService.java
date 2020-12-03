package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 02:23:15
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 会员注册
     * @param memberRegistVo
     */
    void regist(MemberRegistVo memberRegistVo);

    // 校验用户名是否已存在
    void checkUsernameUnique(String userName) throws UserNameExistException;
    // 校验手机是否已存在
    void checkPhonelUnique(String phone) throws PhoneExistException;

    /**
     * 会员登录
     * @param memberLoginVo
     * @return
     */
    MemberEntity login(MemberLoginVo memberLoginVo);

    /**
     * 会员社交登录或注册
     * @param socialUserVo
     * @return
     */
    MemberEntity login(SocialUserVo socialUserVo) throws Exception;
}

