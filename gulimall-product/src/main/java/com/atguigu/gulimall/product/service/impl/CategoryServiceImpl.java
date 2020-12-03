package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查出所有分类以及子分类，以树形结构组装
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {

        // 1、查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2、组装树形的父子树形结构
        //  ①找到所有的一级分类
        //  ②递归查询所有子菜单

        List<CategoryEntity> level1Menus = categoryEntities.stream().filter((categoryEntity) -> { // 进行过滤 得到一级分类
            return categoryEntity.getParentCid() == 0; // 如果分类的父id==0 则返回
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> {
            // menu1前面的菜单 menu2后面的菜单 对比排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public boolean removeByMuneByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        int i = baseMapper.deleteBatchIds(asList);
        return i > 0;
    }

    /**
     * 根据三级分类id，找到三级分类的完整路径
     *
     * @param id 三级分类id
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long id) {
        List<Long> list = new ArrayList<Long>();

        List<Long> paths = findParentPath(id, list);
        // 逆序集合
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLeve1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
//     allEntries = true 删除category 分区的所有缓存 批量清除
    @CacheEvict(value = {"category"}, allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        // 更新关联表
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 查询所有一级分类
     * <p>
     * 1、每一个需要缓存的数据我们都要来指定放到哪个名字的缓存；【按照业务类型来划分取名】
     * 2、@Cacheable({"category"})
     * 当前方法的结果需要缓存 如果缓存中有，方法不调用；
     * 如果缓存中没有，会调用该方法，最后将方法的结果放入缓存
     * 3、默认行为
     * 1)、默认缓存不过期
     * 4、自定义
     * 1)、指定缓存生成指定的key
     * 2)、指定缓存的过期时间  配置文件修改ttl
     * 3)、将缓存的value保存为json格式
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLeve1Categorys() {
        System.out.println("CategoryServiceImpl.getLeve1Categorys (获取一级分类)调用了");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntityList;
    }

    /**
     * 查询前台需要显示的分类数据 - 使用spring cache框架缓存
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        System.out.println("查询了数据库。。。。");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 1、查询所有一级分类
        List<CategoryEntity> leve1Categorys = getParentCid(categoryEntityList, 0L);

        // 2、封装需要的数据
        Map<String, List<Catelog2Vo>> map = leve1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            // 根据每个一级分类，查询到他的二级分类
            List<CategoryEntity> category2EntityList = getParentCid(categoryEntityList, v.getCatId());

            // 抽取出前台需要的的二级分类vo
            List<Catelog2Vo> l2VoList = null;

            if (category2EntityList != null) {

                l2VoList = category2EntityList.stream().map(l2 -> {
                    // 当前一级分类的2级分类vo
                    Catelog2Vo l2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    // 封装当前二级分类vo的三级分类vo
                    List<CategoryEntity> l3EntityList = getParentCid(categoryEntityList, l2.getCatId());
                    List<Catelog2Vo.Catelog3Vo> l3VoList = null;
                    if (l3EntityList != null) {
                        l3VoList = l3EntityList.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo l3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return l3Vo;
                        }).collect(Collectors.toList());
                    }
                    // 给二级分类vo设置封装好的三级分类vo
                    l2Vo.setCatalog3List(l3VoList);
                    return l2Vo;
                }).collect(Collectors.toList());
            }
            return l2VoList;
        }));

        return map;
    }




    /**
     * 查询前台需要显示的分类数据 - 优化升级加入redis缓存
     *
     * @return
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //  产生了堆外存泄漏：OutDirceMemoryError
        // 1、springboot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信
        // 2、lettuce的bug导致netty的堆内存溢出 （-Xmx300m  如果没有指定堆外存， 默认使用 -Xmx300）
        //      -Dio.netty.maxDirectMemory进行设置
        // 解决方案： 不能单独-Dio.netty.maxDirectMemory进行设置 调大堆外内存
        //          ① 升级lettuce客户端      ② 切换使用jedis
        /**
         * 优化一：将数据库的多次查询变为一次
         * 优化二：将查到的数据放入redis缓存
         */

        /**
         * 优化：
         *      缓存穿透： 设置空结果缓存
         *
         *      缓存雪崩： 设置缓存随机的过期时间
         *
         *      缓存击穿： 加锁
         */

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        Map<String, List<Catelog2Vo>> result = null;
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("未命中缓存。。。。开始访问查询数据库");
            result = getCatalogJsonFromDBWithRedissonLock();

            return result;
        }
        System.out.println("查询前台需要显示的分类数据  命中缓存！！！！");
        result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return result;
    }

    /**
     * 查询前台需要显示的分类数据 - redisson框架实现分布式锁
     * <p>
     * 缓存中的数据如何和数据库的保持一致
     * ① 双写模式
     * ② 失效模式
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {


        // 1、占分布式锁，去reids占坑
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock(); // 阻塞等待

        // 加锁成功...执行业务
        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            // 访问数据库
            dataFromDB = getCatalogJSONDataFromDB();
        } finally {
            lock.unlock(); // 解锁
        }

        return dataFromDB;
    }





    /**
     * 从数据库获取分类数据
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJSONDataFromDB() {

        // 线程进来得到锁以后，应该再去缓存确定一次，如果没有数据才需要继续访问数据库查询
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");

        if (!StringUtils.isEmpty(catalogJSON)) { // 如果缓存中数据不为空 则返回缓存中的数据
            System.out.println("CategoryServiceImpl.getCatalogJsonFromDB 查询前台需要显示的分类数据（catalogJSON）命中缓存！！！！");
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return result;
        }
        System.out.println("查询了数据库。。。。");
        /**
         * 优化一：将数据库的多次查询变为一次
         */
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);


        // 1、查询所有一级分类
        List<CategoryEntity> leve1Categorys = getParentCid(categoryEntityList, 0L);

        // 2、封装需要的数据
        Map<String, List<Catelog2Vo>> map = leve1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            // 根据每个一级分类，查询到他的二级分类
            List<CategoryEntity> category2EntityList = getParentCid(categoryEntityList, v.getCatId());

            // 抽取出前台需要的的二级分类vo
            List<Catelog2Vo> l2VoList = null;

            if (category2EntityList != null) {

                l2VoList = category2EntityList.stream().map(l2 -> {
                    // 当前一级分类的2级分类vo
                    Catelog2Vo l2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    // 封装当前二级分类vo的三级分类vo
                    List<CategoryEntity> l3EntityList = getParentCid(categoryEntityList, l2.getCatId());
                    List<Catelog2Vo.Catelog3Vo> l3VoList = null;
                    if (l3EntityList != null) {
                        l3VoList = l3EntityList.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo l3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return l3Vo;
                        }).collect(Collectors.toList());
                    }
                    // 给二级分类vo设置封装好的三级分类vo
                    l2Vo.setCatalog3List(l3VoList);
                    return l2Vo;
                }).collect(Collectors.toList());
            }
            return l2VoList;
        }));

        // 查到结果，先放入缓存 在释放锁
        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return map;
    }


    /**
     * 查询前台需要显示的分类数据 - 分布式锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {

        // 抢占分布式锁 setIfAbsent --> NX 不存在才占坑 EX 自动过期时间
        // 设置redis锁的自动过期时间 - 防止出现异常、服务崩塌等各种情况，没有执行删除锁操作导致的死锁问题
        // !!! 设置过期时间和加锁必须是同步的、原子的
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        System.out.println(lock);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            // 加锁成功...执行业务
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                // 访问数据库
                dataFromDB = getCatalogJSONDataFromDB();
            } finally {
                // 获取对比值和对比成功删除锁也是要同步的、原子的执行  参照官方使用lua脚本解锁
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }

            return dataFromDB;
        } else {
            // 加锁失败休眠一段时间...重试获取锁
            System.out.println("获取分布式锁失败...等待重试");
            // 重试的频率太快会导致内存溢出
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock(); // 自旋的方式

        }

    }

    /**
     * 查询前台需要显示的分类数据 - 本地锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {

        // TODO 本地锁 synchronized 进程锁 锁不住分布式的服务
        // 只要是同一把锁，就可以锁住需要这个锁的所有线程
        // 1、synchronized (this) springboot所有的组件在容器中都是单例的
        synchronized (this) {

            return getCatalogJSONDataFromDB();
        }


    }

    /**
     * 根据分类的parent_cid获取分类数据
     *
     * @param categoryEntityList
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntityList, Long parentCid) {

        List<CategoryEntity> collect = categoryEntityList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }


    /**
     * 递归查询分类路径
     *
     * @param id   分类id
     * @param list 存储分类id的集合
     * @return [三级, 二级, 一级]
     */
    private List<Long> findParentPath(Long id, List<Long> list) {
        // 存储当前分类的id
        list.add(id);
        CategoryEntity categoryEntity = this.getById(id);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), list);
        }
        return list;
    }

    /**
     * 递归查询所有菜单的子菜单
     *
     * @param root 当前菜单
     * @param all  所有菜单
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity) -> {
            // 递归找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 菜单排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}