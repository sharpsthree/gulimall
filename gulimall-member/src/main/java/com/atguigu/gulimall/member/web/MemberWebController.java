package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/8 17:44
 * @Version 1.0
 **/
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model) {


        HashMap<String, Object> map = new HashMap<>();
        map.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(map);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);

        return "orderList";
    }
}
