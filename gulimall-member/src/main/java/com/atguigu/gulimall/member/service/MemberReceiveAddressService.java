package com.atguigu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 02:23:16
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据会员id查询收货地址
     * @param memberId
     * @return
     */
    List<MemberReceiveAddressEntity> getAddresses(Long memberId);
}

