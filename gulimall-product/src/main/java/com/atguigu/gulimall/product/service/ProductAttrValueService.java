package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author é²ç­ä¸ä¼é£
 * @email know390503802@qq.com
 * @date 2020-04-01 23:12:37
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存spu的规格参数;pms_product_attr_value
     * @param collect
     */
    void saveProductAttr(List<ProductAttrValueEntity> collect);

    /**
     * 获取spu规格
     * @param spuId 商品spuId
     * @return
     */
    List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId);

    /**
     * 根据spu修改商品spu信息 修改商品规格
     * @param spuId
     * @param entities
     */
    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

