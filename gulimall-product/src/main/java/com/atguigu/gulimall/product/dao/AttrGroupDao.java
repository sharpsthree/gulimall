package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    /**
     * 根据spuid获取所有分组&关联属性
     * @param catalogId
     * @param spuId
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("catalogId") Long catalogId, @Param("spuId") Long spuId);
}
