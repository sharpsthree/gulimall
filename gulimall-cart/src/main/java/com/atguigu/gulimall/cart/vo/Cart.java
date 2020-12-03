package com.atguigu.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 整个购物车
 * @Author 鲁班不会飞
 * @Date 2020/4/29 07:55
 * @Version 1.0
 **/
public class Cart {

    private List<CartItem> items;

    private Integer countNum; // 商品数量

    private Integer countType; // 商品类型数量

    private BigDecimal totalAmount; // 所有选中商品的合计价格

    private BigDecimal reduce = new BigDecimal("0.00"); // 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    /**
     * 根据每个购物项的商品数量计算商品的总数量
     * @return
     */
    public Integer getCountNum() {
        int count = 0;
        if (this.items != null && this.items.size() > 0) {
            for (CartItem item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 根据购物项的数量得到商品类型的数量
     * @return
     */
    public Integer getCountType() {
        int count = 0;
        if (this.items != null && this.items.size() > 0) {
            for (CartItem item : this.items) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * 计算所有商品总价减去优惠价后的总价格
     * @return
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 1、计算购物项总价
        if (this.items != null && this.items.size() > 0) {
            for (CartItem item : this.items) {
                if (item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice); // 叠加每个购物项的总价
                }
            }
        }
        // 1、减去优惠价
        BigDecimal finalAmount = amount.subtract(getReduce());

        return finalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
