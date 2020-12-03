package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {

    /**
     * 添加购物车
     * @param skuId 商品skuId
     * @param num 商品数量
     * @return
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 根据skuId获取购物车里购物项
     * @param skuId 商品skuId
     * @return CartItem 购物项
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车数据
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 修改购物项的勾选状态
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物项的数量
     * @param skuId
     * @param count
     */
    void countItem(Long skuId, Integer count);

    /**
     * 删除购物项数据
     * @param skuId
     */
    void delItem(Long skuId);

    /**
     * 获取当前登录用户选中的购物项 - 订单服务
     * @return
     */
    List<CartItem> currentUserCartItems();
}
