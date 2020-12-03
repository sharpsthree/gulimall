package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/1 14:24
 * @Version 1.0
 **/
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 跳转到结算页
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        // 获取订单确认页需要的数据
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 下单
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {

        SubmitOrderRespVo submitOrderRespVo = orderService.submitOrder(orderSubmitVo);
        if (submitOrderRespVo.getCode() == 0) {
            model.addAttribute("submitOrderResp", submitOrderRespVo);
            return "pay";
        } else {
            String msg = "下单失败";
            switch (submitOrderRespVo.getCode()) {
                case 1:
                    msg += "订单信息过期，稍后再试";
                    break;
                case 2:
                    msg += "订单商品价格发生变化";
                    break;
                case 3:
                    msg += "商品库存不足";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}

