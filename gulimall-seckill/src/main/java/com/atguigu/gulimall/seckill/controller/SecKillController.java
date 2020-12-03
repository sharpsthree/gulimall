package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedsTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/11 15:38
 * @Version 1.0
 **/
@Controller
public class SecKillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀的商品信息
     * @return
     */
    @GetMapping("/currentSecKillSkus")
    @ResponseBody
    public R getCurrentSecKillSkus() {
        List<SecKillSkuRedsTo> list = seckillService.getCurrentSecKillSkus();
        return R.ok().data(list);
    }

    /**
     * 查询某个sku的秒杀信息
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedsTo secKillSkuRedsTo = seckillService.getSkuSecKillInfo(skuId);
        return R.ok().data(secKillSkuRedsTo);
    }

    /**
     * http://seckill.gulimall.com/kill?killId=1_1&key=79d777f685a247d7a9d2ea261c9c8430&num=1
     * 秒杀商品
     * @param killId 场次id_商品skuid
     * @param key 秒杀需要携带的令牌
     * @param num 秒杀的数量
     * @return
     */
    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {

        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }



}
