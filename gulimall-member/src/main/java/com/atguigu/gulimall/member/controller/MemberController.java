package com.atguigu.gulimall.member.controller;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-02 02:23:15
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    /**
     * 会员社交登录
     * @param socialUserVo
     * @return
     */
    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUserVo socialUserVo) throws Exception {
        MemberEntity memberEntity = memberService.login(socialUserVo);
        if (memberEntity != null) {
            return R.ok().data(memberEntity);
        } else {
            return R.error(BizCodeEnume.LOGINACCT_PASSSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }


    /**
     * 会员注册
     * @param memberRegistVo
     * @return
     */
    @RequestMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){

        try {
            memberService.regist(memberRegistVo);
        } catch (UserNameExistException e) {
            R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException e) {
            R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 会员登录
     * @param memberLoginVo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo) {
        MemberEntity memberEntity = memberService.login(memberLoginVo);
        if (memberEntity != null) {
            return R.ok().data(memberEntity);
        } else {
            return R.error(BizCodeEnume.LOGINACCT_PASSSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }


    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("鲁班不会飞");

        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member",memberEntity).put("memberCoupons",memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
