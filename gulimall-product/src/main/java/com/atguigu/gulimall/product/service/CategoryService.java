package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author lubancantfly
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查出所有分类以及子分类，以树形结构组装
     * @return
     */
    List<CategoryEntity> listWithTree();

    boolean removeByMuneByIds(List<Long> asList);

    /**
     * 根据三级分类id，找到三级分类的完整路径
     * @param id 三级分类id
     * @return
     */
    Long[] findCatelogPath(Long id);

    void updateCascade(CategoryEntity category);

    /**
     * 查询所有一级分类
     * @return
     */
    List<CategoryEntity> getLeve1Categorys();

    /**
     * 查询前台需要的分类信息
     * @return
     */
    Map<String, List<Catelog2Vo>> getCatalogJson();
}

