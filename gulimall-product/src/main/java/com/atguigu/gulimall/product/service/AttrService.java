package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrVo(AttrVo attrVo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrDetail(Long attrId);

    void updateAttrDetail(AttrVo attr);

    /**
     * 根据分组id查询关联的所有属性
     * @param attrgroupId
     * @return
     */
    List<AttrEntity> getAttrRelation(Long attrgroupId);

    void deleteAttrRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    /**
     * 获取分组未关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * 指定的的所有属性集合里，查询出可被检索的属性
     * @param attrIds 用来查询的属性id集合
     * @return
     */
    List<Long> selectSearchAttrs(List<Long> attrIds);
}

