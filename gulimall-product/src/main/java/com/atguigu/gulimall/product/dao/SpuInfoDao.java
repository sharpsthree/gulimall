package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    /**
     * 修改spu的状态
     * @param spuId 商品spuid
     * @param code spu的状态
     */
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
