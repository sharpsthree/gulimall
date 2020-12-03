package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据分类id分页查询
     * @param params
     * @param catelogId
     * @return
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    /**
     * 根据spuid获取所有分组&关联属性
     * @param catalogId
     * @param spuId
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long catalogId, Long spuId);
}

