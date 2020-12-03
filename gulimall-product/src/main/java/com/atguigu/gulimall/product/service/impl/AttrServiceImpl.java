package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.pms.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存属性的基本信息和关联的表信息
     * @param attrVo
     */
    @Transactional
    @Override
    public void saveAttrVo(AttrVo attrVo) {

        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);

        // 保存基本信息
        this.save(attrEntity);


        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null) {
            // 保存关联关系的表信息
            // 查询是否已经有该条关联关系了
            attrAttrgroupRelationDao.selectById(attrEntity.getAttrId());

            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrSort(0);
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    /**
     * 获取分类规格参数
     * @param params
     * @param catelogId
     * @param type
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {

        // 定义查询条件
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ARRT_TYPE_SALE.getCode());

        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and( (obj)->{
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // 开始处理分页信息一集表关联数据
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> respVoList = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            if ("base".equalsIgnoreCase(type)) { // 如果本次操作查询的是规格信息 才需要查询分组关联信息
                // 设置分组的名字
                // 1、查询当前属性的groupid pms_attr_attrgroup_relation
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVoList);

        return pageUtils;
    }

    /**
     * 查询详细信息
     * @param attrId
     * @return
     */
    @Cacheable(value = "attr", key = "'attrinfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrDetail(Long attrId) {

        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);

        BeanUtils.copyProperties(attrEntity,attrRespVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) { // 如果当前的基本属性才需要查询属性关联分组信息
            // 关联查询分组的信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrAttrgroupRelationEntity != null) {
                attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 设置分类的完整路径
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrRespVo.setGroupName(categoryEntity.getName());
        }

        return attrRespVo;
    }

    /**
     * 保存详细信息
     * @param attr
     */
    @Transactional
    @Override
    public void updateAttrDetail(AttrVo attr) {
        // 保存基本信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);


        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) { // 如果当前的基本属性才需要修改属性关联分组信息
            QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId());

            //由于属性在新增的时候可能没有选择分组，所以要先判断属性和分组关联表是否有当前需要修改的这条信息
            // 1、如果没有,执行新增操作，如果有则执行修改操作

            Integer count = attrAttrgroupRelationDao.selectCount(wrapper);

            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            if (count > 0) {
                // 修改分组关联信息
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, wrapper);
            } else {
                attrAttrgroupRelationEntity.setAttrSort(0);
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }

    }

    /**
     * 根据分组id查询关联的所有属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getAttrRelation(Long attrgroupId) {

        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIdList = attrAttrgroupRelationEntityList.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIdList == null || attrIdList.size() == 0) {
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(attrIdList);

        return (List<AttrEntity>) attrEntities;
    }

    @Override
    public void deleteAttrRelation(AttrGroupRelationVo[] attrGroupRelationVos) {

        // attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", 1L).eq("attr_group_id", 1L));

        List<AttrAttrgroupRelationEntity> relationEntityList = Arrays.asList(attrGroupRelationVos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        attrAttrgroupRelationDao.deleteBatchRelation(relationEntityList);


    }

    /**
     * 获取分组未关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1、当前分组只能关联自己所属分类里的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2、当前分组只能关联别的分组没有引用的属性
        // 查询当前分类下的所有分组
        List<AttrGroupEntity> groupEntityList = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 获取这些分组的id
        List<Long> groupIds = groupEntityList.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        // 获取这些分组信息
        List<AttrAttrgroupRelationEntity> AttrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));

        // 获取这些分组属性id
        List<Long> attrIds = AttrAttrgroupRelationEntity.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        // 1、获取，除去当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w)->{
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    /**
     * 指定的的所有属性集合里，查询出可被检索的属性
     * @param attrIds 用来查询的属性id集合
     * @return
     */
    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {

        return this.baseMapper.selectSearchAttrs(attrIds);
    }

}