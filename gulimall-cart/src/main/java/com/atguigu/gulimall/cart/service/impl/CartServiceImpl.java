package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/4/29 08:26
 * @Version 1.0
 **/
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimall:cart:";


    /**
     * // TODO 不需要再返回 CartItem
     * 添加购物车
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 根据商品的skuId查询redis中存储的数据库， 并判断是否存在这个购物车
        String result = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(result)) {
            // redis中不存在本次添加到购物车的商品，则执行添加新商品到购物车操作
            CartItem cartItem = new CartItem();

            // 异步任务① 调用远程商品服务，根据skuId查询商品sku信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                // 1、调用远程商品服务，根据skuId查询商品sku信息
                R r = productFeignService.getSkuInfo(skuId);
                if (r.getCode() == 0) {
                    SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    cartItem.setCheck(true);
                    cartItem.setCount(num);
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setSkuId(skuId);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                }
            }, executor);
            // 异步任务② 调用远程商品服务，查询商品sku销售组合信息
            CompletableFuture<Void> getSkuSaleAttrsTask = CompletableFuture.runAsync(() -> {
                // 3、远程查询商品sku组合信息
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                if (skuSaleAttrValues != null && skuSaleAttrValues.size() > 0) {
                    cartItem.setSkuAttr(skuSaleAttrValues);
                }
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrsTask).get(); // 阻塞等待上面的两个异步任务都完成，再往redis存储数据

            String jsonCartItem = JSON.toJSONString(cartItem); // 将封装的购物车对象转换成json字符串存储
            cartOps.put(skuId.toString(), jsonCartItem); // 存储到redis
            return cartItem;
        } else {
            // redis中存在本次添加到购物车的商品，则执行修改商品数量操作
            CartItem cartItem = JSON.parseObject(result, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            String jsonCartItem = JSON.toJSONString(cartItem); // 将封装的购物车对象转换成json字符串存储
            cartOps.put(skuId.toString(), jsonCartItem); // 存储到redis
            return cartItem;

        }
    }

    /**
     * 获取要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        // 判断是临时购物车还是登录用户的购物车
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> boundHashOps = stringRedisTemplate.boundHashOps(cartKey);
        return boundHashOps;
    }


    /**
     * 根据skuId获取购物车里购物项
     * @param skuId 商品skuId
     * @return CartItem 购物项
     */
    @Override
    public CartItem getCartItem(Long skuId) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String result = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(result, CartItem.class);
        return cartItem;
    }

    /**
     * 获取购物车数据
     * 登录后用户的购物车或未登录的临时购物车
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        // 判断用户是否登陆了 threadLocal同一线程共享数据
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 登录了，获取登录用户的购物车和临时购物车进行合并
            String cartKey = CART_PREFIX + userInfoTo.getUserId();

            // 1、判断临时购物车是否有数据
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size() > 0) {
                // 临时购物车有数据，进行合并
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                // 合并完成 清除临时购物车数据
                clearCart(tempCartKey);
            }
            // 2、合并后，再获取登录后用户的购物车
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            // 未登录 获取临时购物车所有购物项
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }
        return cart;
    }

    /**
     * 获取某个购物车的所有购物项
     * @param cartKey 购物车令牌
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItems = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItems;
        }
        return null;
    }

    /**
     * 清空购物车数据
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 修改购物项的勾选状态
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {

        // 根据skuId redis中购物项，并修改购物项的选中状态
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1? true:false);

        // 重新写入reids
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String strJson = JSON.toJSONString(cartItem);

        cartOps.put(skuId.toString(), strJson);
    }

    /**
     * 修改购物项的数量
     * @param skuId
     * @param count
     */
    @Override
    public void countItem(Long skuId, Integer count) {

        // 根据skuId redis中购物项，并修改购物项的数量
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(count);

        // 重新写入reids
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String strJson = JSON.toJSONString(cartItem);

        cartOps.put(skuId.toString(), strJson);
    }

    /**
     * 删除购物项数据
     * @param skuId
     */
    @Override
    public void delItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获取当前登录用户选中的购物项
     * @return
     */
    @Override
    public List<CartItem> currentUserCartItems() {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            // 过滤选中的购物项 才需要返回
            List<CartItem> finalCartItems = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item -> {
                        // 更新商品的最新最新的价格
                        R r = productFeignService.getPrice(item.getSkuId());
                        String newPrice = (String) r.get("data");
                        item.setPrice(new BigDecimal(newPrice));
                        return item;
                    })
                    .collect(Collectors.toList());

            return finalCartItems;
        }

    }
}
