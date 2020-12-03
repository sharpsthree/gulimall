package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Test
    public void getSaleAttrsBySpuId() {

        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(2l);

        for (SkuItemSaleAttrVo skuItemSaleAttrVo : saleAttrsBySpuId) {
            System.out.println(skuItemSaleAttrVo);
        }

    }


    @Test
    public void getAttrGroupWithAttrsBySpuIdTest() {

        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(225L, 2L);

        for (SpuItemAttrGroupVo spuItemAttrGroupVo : attrGroupWithAttrsBySpuId) {
            System.out.println(spuItemAttrGroupVo);
        }

    }

    @Test
    public void redissonTest() {
        System.out.println(redissonClient);
    }

    @Test
    public void stringRedisTemplateTest() {

        stringRedisTemplate.opsForValue().set("hello","world");
        System.out.println("保存了数据");

        String hello = stringRedisTemplate.opsForValue().get("hello");
        System.out.println("刚刚保存的值为" +  hello);

    }

    @Test
    public void redisTemplateTest() {

        BrandEntity brand = new BrandEntity();
        brand.setName("哈哈哈哈哈测试redis");

        redisTemplate.opsForValue().set("brand", brand);
        System.out.println("保存了数据");

        BrandEntity obj = (BrandEntity) redisTemplate.opsForValue().get("brand");
        System.out.println("刚刚保存的值为" +  obj.getName());

    }

    @Test
    public void categoryPathTest() {

        Long[] catelogPath = categoryService.findCatelogPath( 225L);
        for (Long aLong : catelogPath) {
            System.out.println(aLong);
        }
    }

//    @Autowired
//    OSSClient ossClient;
//
//    @Test
//    public void ossTest() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
////        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
////        String accessKeyId = "LTAI4Fec4n99HjGECZGjxVx8";
////        String accessKeySecret = "By1KnhZRjUR8imapMzYTqR6m0F1s1n";
//
//        // 创建OSSClient实例。
////        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("/Users/crx/Downloads/2020电商/1-分布式基础_全栈开发篇/docs/pics/e3284f319e256a5d.jpg");
//        ossClient.putObject("gulimall-lubancantfly", "e3284f319e256a5d.jpg", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传成功");
//    }

    @Test
    public void contextLoads() {

        BrandEntity brandEntity1 = new BrandEntity();
        brandEntity1.setName("华为");
        brandService.save(brandEntity1);

        List<BrandEntity> brands = brandService.list();
        brands.forEach( (brandEntity -> {
            System.out.println(brandEntity);
        }));
    }

}
