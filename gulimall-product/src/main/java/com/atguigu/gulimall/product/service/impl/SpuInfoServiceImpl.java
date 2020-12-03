package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.pms.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesServiceImpl spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    // 当前线程共享同样的数据 商品id
    ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
             new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        this.saveSpuInfoDesc(spuSaveVo);

        //3、保存spu的图片集 pms_spu_images
        saveSpuImages(spuSaveVo);

        //4、保存spu的规格参数;pms_product_attr_value
        saveProductAttrValue(spuSaveVo);

        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(threadLocal.get());

        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.debug("远程保存spu信息积分失败");
        }

        // TODO 优化代码
        //6、保存当前spu对应的所有sku信息
        //  6.1）、sku的基本信息；pms_sku_info
        List<Skus> skus = spuSaveVo.getSkus();

        if (skus != null && skus.size() > 0) {
            skus.forEach( item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());

                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //6.2）、sku的图片信息；pms_sku_image
                List<SkuImagesEntity> imagesEntityList = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());

                    return skuImagesEntity;
                }).filter( imagesEntity -> {
                    //返回true就是需要，false就是剔除  过滤掉未勾选的图片
                    return !StringUtils.isEmpty(imagesEntity.getImgUrl());
                }).collect(Collectors.toList());

                skuImagesService.saveBatch(imagesEntityList);

                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //6.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }

    }


    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and( (w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {

        // 查出当前spuId对应的所有sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        // 把skus里的skuid重新封装成一个集合 - 为了远程查询sku的库存
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 4、查询当前sku的所有可以被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);


        // 过滤掉不看可被检索的属性 并且封装成es需要的Attrs
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(attr -> {
            return idSet.contains(attr.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            //  1、发送远程请求，查询库存系统里是否有库存
            R r = wareFeignService.getSkusHasStock(skuIds);

            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));

        } catch (Exception e) {
            log.error("SpuInfoServiceImpl.up 商品上架时远程调用库存服务查询异常 --> 原因{}", e);
        }

        // 封装每个sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            // 组装skuinfo里名称不匹配的和没有的字段
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            // 设置是否有库存
            if (finalStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // TODO 2、热度评分 (暂时默认0)
            esModel.setHotScore(0L);

            // 3、查询品牌和分类的名字
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());


        // final 将收集到的数据 保存到es
        R r = searchFeignService.productStstusUp(collect);

        if (r.getCode() == 0) { // 远程gulimall-search服务想es索引数据成功 修改商品spu上架状态
            // TODO 修改当前spu的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatusEnum.UP_SPU.getCode());
        } else { // 失败
            // TODO 重复调用 接口幂等性 重试机制


        }


    }

    // TODO 代码优化
    /**
     * 1、保存商品spu基本信息  pms_spu_info
     * @param infoEntity
     */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
        log.debug("本次保存商品的id：{}", infoEntity.getId());
        // 存储刚刚存入的商品id
        threadLocal.set(infoEntity.getId());


        log.debug("当前线程....{}---->{} ------- 保存商品spu基本信息",Thread.currentThread().getId(),Thread.currentThread().getName());
    }

    /**
     * 2、保存Spu的描述图片 pms_spu_info_desc
     * @param spuSaveVo
     */
    private void saveSpuInfoDesc(SpuSaveVo spuSaveVo) {
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        // 获取当前线程中存储的商品id
        descEntity.setSpuId(threadLocal.get());
        descEntity.setDecript(String.join(",",decript));

        spuInfoDescService.saveSpuInfoDesc(descEntity);

        log.debug("当前线程....{}---->{} ------- 保存商品Spu的描述图片信息",Thread.currentThread().getId(),Thread.currentThread().getName());

    }

    /**
     * 3、保存spu的图片集 pms_spu_images
     * @param spuSaveVo
     */
    private void saveSpuImages(SpuSaveVo spuSaveVo) {

        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(threadLocal.get(),images);

        log.debug("当前线程....{}---->{} ------- 保存商品spu的图片集信息",Thread.currentThread().getId(),Thread.currentThread().getName());

    }

    /**
     * 4、保存spu的规格参数;pms_product_attr_value
     * @param spuSaveVo
     */
    private void saveProductAttrValue(SpuSaveVo spuSaveVo) {
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();

        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            // 根据属性id 获取属性名
            AttrEntity id = attrService.getById(attr.getAttrId());

            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(threadLocal.get());

            return valueEntity;
        }).collect(Collectors.toList());
        // TODO 优化代码
        productAttrValueService.saveProductAttr(collect);

    }

    /**
     * 根据skuId查询spu信息
     * @param skuId
     * @return
     */
    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {

        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);

        SpuInfoEntity spuInfoEntity = this.baseMapper.selectById(skuInfoEntity.getSpuId());

        return spuInfoEntity;
    }

}