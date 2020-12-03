package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/8 14:30
 * @Version 1.0
 **/
@Controller
public class PayWebController {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 订单支付 - 展示支付页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPayInfo(orderSn);

        // 支付宝的支付返回的是一个支付的页面
        String payPage = alipayTemplate.pay(payVo);

        return payPage;
    }
}
