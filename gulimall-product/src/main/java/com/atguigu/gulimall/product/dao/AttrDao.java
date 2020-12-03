package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    /**
     * 指定的的所有属性集合里，查询出可被检索的属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrs(@Param("attrIds") List<Long> attrIds);
}
