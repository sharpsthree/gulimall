package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 会员注册
     * @param memberRegistVo
     */
    @Override
    public void regist(MemberRegistVo memberRegistVo) {

        // 检查用户名和密码是否唯一 为了让controller能感知异常，使用异常机制
        checkUsernameUnique(memberRegistVo.getUserName());
        checkPhonelUnique(memberRegistVo.getPhone());


        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId()); // 设置默认等级
        memberEntity.setUsername(memberRegistVo.getUserName());
        memberEntity.setMobile(memberRegistVo.getPhone());
        memberEntity.setNickname(memberRegistVo.getUserName());
        // 密码spring自带的Md5盐值加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(memberRegistVo.getPassword());
        memberEntity.setPassword(encode);

        // TODO 其他默认信息

        this.baseMapper.insert(memberEntity);
    }

    /**
     * 检查用户名是否已存在
     * @param userName
     */
    @Override
    public void checkUsernameUnique(String userName) throws UserNameExistException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    /**
     * 检查手机号是否已存在
     * @param phone
     */
    @Override
    public void checkPhonelUnique(String phone) throws PhoneExistException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    /**
     * 会员登录
     * @param memberLoginVo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        // 1、数据库查询该用户的密码
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", memberLoginVo.getLoginacct())
                .or().eq("mobile", memberLoginVo.getLoginacct()));
        if (memberEntity == null) {
            return null;
        } else {
            String passwordDB = memberEntity.getPassword(); // 获取数据库中加密的密码
            // spring家加密机制 让提交的密码与数据库的密文密码进行匹配
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(memberLoginVo.getPassword(), passwordDB);
            if (matches) { // 匹配成功
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    /**
     * 会员社交登录或注册
     * @param socialUserVo
     * @return
     */
    @Override
    public MemberEntity login(SocialUserVo socialUserVo) throws Exception {
        String uid = socialUserVo.getUid();
        // 1、判断当前社交用户是否在本系统已经注册过
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) { // 直接登录
            MemberEntity memberEntity_update = new MemberEntity();
            memberEntity_update.setId(memberEntity.getId());
            memberEntity_update.setAccessToken(socialUserVo.getAccess_token());
            memberEntity_update.setExpiresIn(socialUserVo.getExpires_in());
            this.baseMapper.updateById(memberEntity_update);

            memberEntity.setAccessToken(socialUserVo.getAccess_token());
            memberEntity.setExpiresIn(socialUserVo.getExpires_in());
            return memberEntity;
        } else { // 注册
            MemberEntity regist = new MemberEntity();

            try {
                // 查询当前社交用户的信息（昵称、性别等）
                HashMap<String, String> map = new HashMap<>();
                map.put("access_token", socialUserVo.getAccess_token());
                map.put("uid", socialUserVo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), map);
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 获取用户信息成功 构建系统会员信息
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String nickname = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    regist.setNickname(nickname);
                    regist.setGender("m".equalsIgnoreCase(gender)? 1 : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 上面的try是为了防止远程查询用户信息失败时，也不影响本次注册！！！
            regist.setSocialUid(socialUserVo.getUid());
            regist.setAccessToken(socialUserVo.getAccess_token());
            regist.setExpiresIn(socialUserVo.getExpires_in());
            this.baseMapper.insert(regist); // 将构建的数据写入数据库
            return regist;
        }
    }
}