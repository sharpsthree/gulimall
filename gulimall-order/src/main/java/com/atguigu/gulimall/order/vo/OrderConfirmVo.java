package com.atguigu.gulimall.order.vo;

/**
 * @Description 订单确认页封装对象
 * @Author 鲁班不会飞
 * @Date 2020/5/1 15:08
 * @Version 1.0
 **/

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    @Setter @Getter
    List<MemberAddressVo> addressList; // 会员收货地址列表

    @Setter @Getter
    List<OrderItemVo> items; // 所有选中的购物项

    @Setter @Getter
    Integer integration; // 会员的积分信息

  //  BigDecimal total; // 订单总额

    //BigDecimal payPrice; // 应付价格
    @Setter @Getter
    Map<Long, Boolean> stocks;

    @Setter @Getter
    String orderToken; // 订单防重令牌


    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                BigDecimal itemTotalPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(itemTotalPrice);
            }
        }
        return sum;
    }

    // TODO 未实现优惠
    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                BigDecimal itemTotalPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(itemTotalPrice);
            }
        }
        return sum;
    }

}
