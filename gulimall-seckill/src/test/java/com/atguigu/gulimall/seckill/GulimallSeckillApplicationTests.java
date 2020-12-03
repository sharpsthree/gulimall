package com.atguigu.gulimall.seckill;

import org.junit.Test;

import java.math.BigDecimal;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class GulimallSeckillApplicationTests {

    @Test
    public void contextLoads() {

        Integer n = 5;
        BigDecimal bignum1 = new BigDecimal("10");
        BigDecimal bignum2 = new BigDecimal(n + "");
        BigDecimal bignum3 = null;

        // 加法
        bignum3 =  bignum1.add(bignum2);
        System.out.println("和 是：" + bignum3);

        // 减法
        bignum3 = bignum1.subtract(bignum2);
        System.out.println("差  是：" + bignum3);

        // 乘法
        bignum3 = bignum1.multiply(bignum2);
        System.out.println("积  是：" + bignum3);

        // 除法
        bignum3 = bignum1.divide(bignum2);
        System.out.println("商  是：" + bignum3);

        if (bignum1.compareTo(bignum2) == -1) {

        }
    }

}
