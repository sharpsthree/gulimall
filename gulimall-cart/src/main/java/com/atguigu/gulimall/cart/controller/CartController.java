package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/4/29 12:34
 * @Version 1.0
 **/
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 获取购物车数据 - 去购物车页面
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    /**
     * 添加购物车 - 添加成功转发到成功页面（在成功页面查询购物项的信息，防止刷新时重复提交 商品数量增加）
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);

        // RedirectAttributes.addFlashAttribute() 将数据存放在session里面，可以在页面取出，但是只可以取一次
        // RedirectAttributes.addAttribute()放的数据 如果是转发，放到请求域中，如果是重定向则自动拼接到路径后
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 添加购物车成功 - 跳转到成功页面
     * @param skuId
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {

        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

    /**
     * 修改购物项的勾选状态
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 修改购物项的数量
     * @param skuId
     * @param count
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        cartService.countItem(skuId, count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除购物项数据
     * @param skuId
     * @return
     */
    @GetMapping("/delItem")
    public String delItem(@RequestParam("skuId") Long skuId) {
        cartService.delItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 获取当前登录用户选中的购物项
     * @return
     */
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> currentUserCartItems() {
        List<CartItem> cartItems = cartService.currentUserCartItems();
        return cartItems;
    }
}
