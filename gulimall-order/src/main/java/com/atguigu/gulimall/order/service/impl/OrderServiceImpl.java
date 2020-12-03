package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取订单确认页需要的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        /**
         * 解决异步任务拿不到ThreadLocal里的数据
         * 获取之前的请求，让每个异步任务的线程共享ThreadLocal数据
         */
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {

            RequestContextHolder.setRequestAttributes(requestAttributes); // 解决异步任务拿不到ThreadLocal里的数据

            // 1、远程查询所有的收货地址
            List<MemberAddressVo> addresses = memberFeignService.getAddresses(memberRespVo.getId());
            orderConfirmVo.setAddressList(addresses);
        }, executor);

        CompletableFuture<Void> getCartTask = CompletableFuture.runAsync(() -> {

            RequestContextHolder.setRequestAttributes(requestAttributes); // 解决异步任务拿不到ThreadLocal里的数据

            // 2、远程查询购物车所有选中的购物项
            List<OrderItemVo> orderItems = cartFeignService.currentUserCartItems();
            orderConfirmVo.setItems(orderItems);
        }, executor).thenRunAsync(() -> {
            // 查询商品是否有库存
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }

        },executor);


        // 3、用户积分信息
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);

        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);


        CompletableFuture.allOf(getAddressTask, getCartTask).get(); // 阻塞等待异步任务完成

        return orderConfirmVo;
    }

    /**
     * 下单
     * @param orderSubmitVo
     * @return
     */
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {

        orderSubmitVoThreadLocal.set(orderSubmitVo);

        SubmitOrderRespVo submitOrderRespVo = new SubmitOrderRespVo();
        submitOrderRespVo.setCode(0);

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        // 判断本次下单的token和redis存储的token是否一致
        String redisOrderToken = stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());

        //  redis+lua脚本 原子验证令牌防止重复提交攻击
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        //  return 0 失败  1 成功
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);

        if (result == 0) { // 令牌验证失败
            submitOrderRespVo.setCode(1);
            return submitOrderRespVo;
        } else { // 令牌验证成功
            OrderCreateTo order = createOrder();

            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 验价通过

                // 保存订单！！！
                saveOrder(order);
                // 锁库存 有异常回滚
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrderEntity().getOrderSn());

                List<OrderItemVo> collect = order.getItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());

                wareSkuLockVo.setLocks(collect);

                // 远程锁库存！！！
                R r = wareFeignService.orderLocKStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    submitOrderRespVo.setOrderEntity(order.getOrderEntity());
                    // TODO 如果使用的积分优惠，扣减积分
                    //int i = 10/0; // 测试分布式事务，远程锁库存业务回滚

                    // 订单创建成功发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrderEntity());

                    return submitOrderRespVo;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            } else {
                submitOrderRespVo.setCode(2);
                return submitOrderRespVo;
            }
        }



    }


    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {

        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());

        this.save(orderEntity);

        List<OrderItemEntity> items = order.getItems();
        for (OrderItemEntity item : items) {
            orderItemService.getBaseMapper().insert(item);
        }

    }

    /**
     * 创建订单
     * @return
     */
    private OrderCreateTo createOrder() {

        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 生成订单号 mp自带的
        // 时间 ID = Time + ID
        // *<p>例如：可用于商品订单 ID</p>
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setReceiverName(memberRespVo.getNickname());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        // 页面传递的数据
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();


        // 构建所有订单项数据
        List<OrderItemEntity> orderItemEntities = buidOrderItems(orderSn);

        // 计算订单价格等信息
        computerPrice(orderEntity,orderItemEntities);

        orderCreateTo.setOrderEntity(orderEntity);

        orderCreateTo.setItems(orderItemEntities);

        return orderCreateTo;
    }

    /**
     * 计算订单价格等信息
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal totalPrice = new BigDecimal("0.0");
        BigDecimal totalGiftIntegration = new BigDecimal("0.0");
        BigDecimal totalGiftGrowth = new BigDecimal("0.0");

        for (OrderItemEntity itemEntity : orderItemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            totalGiftIntegration = totalGiftIntegration.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
            totalGiftGrowth = totalGiftGrowth.add(new BigDecimal(itemEntity.getGiftGrowth()));
        }

        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice);

        orderEntity.setIntegration(totalGiftIntegration.intValue());
        orderEntity.setGrowth(totalGiftGrowth.intValue());
    }

    /**
     * 构建所有订单项数据
     * @return
     * @param orderSn
     */
    private List<OrderItemEntity> buidOrderItems(String orderSn) {
        // 获取所有的订单项数据
        List<OrderItemVo> currentUserCartItems = cartFeignService.currentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buidOrderItem(cartItem);

                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }
        return null;
    }

    /**
     * 构建某个订单项数据
     * @param cartItem
     * @return
     */
    private OrderItemEntity buidOrderItem(OrderItemVo cartItem) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 商品spu信息
        R r = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

        // sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());

        // 成长值、积分信息
        // TODO 模拟数据
        int giftGrowth = (cartItem.getTotalPrice().intValue()) / 10;
        orderItemEntity.setGiftGrowth(giftGrowth);
        orderItemEntity.setGiftIntegration(giftGrowth);
        // 该商品经过优惠后的分解金额
        // TODO 实现优惠后的价格
        orderItemEntity.setRealAmount(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString())));

        return orderItemEntity;

    }


    /**
     * 根据订单号查询订单状信息
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            // 关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);

            // 发送消息给MQ
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            // 确保每个消息发送成功，给每个消息做好日志记录，保存每个消息的详细信息
            // 定期扫描数据库，重新发送失败的消息
            try {
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPayInfo(String orderSn) {

        OrderEntity order = getOrderByOrderSn(orderSn);

        PayVo payVo = new PayVo();
        // 支付宝可支付的金额是2位小数的
        // .setScale(2, BigDecimal.ROUND_UP) 修改为两位小数，并向上取值
        String totalAmount = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString();
        payVo.setTotal_amount(totalAmount);
        payVo.setOut_trade_no(orderSn);
        List<OrderItemEntity> orderItemList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItem = orderItemList.get(0);
        payVo.setSubject(orderItem.getSkuName());
        payVo.setBody(orderItem.getSkuAttrsVals());

        return payVo;
    }


    /**
     * 查询用户的订单信息
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWhtiItem(Map<String, Object> params) {

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> orderEntityList = page.getRecords().stream().map(orderEntity -> {

            List<OrderItemEntity> items = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderEntity.getOrderSn()));
            orderEntity.setOrderItemEntityList(items);
            return orderEntity;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);

    }

    /**
     * 支付成功修改订单信息
     * @param payAsyncVo 支付宝返回的信息
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {

        // 1、保存交易流水信息
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        // 2、修改订单状态信息
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            // 支付成功
            String orderSn = payAsyncVo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    /**
     * 创建秒杀商品订单
     * TODO 解决：校验问题
     * @param secKillOrderTo
     */
    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
        // TODO 设置详细信息

        // 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderEntity.setMemberId(secKillOrderTo.getMemberId());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal totalPrice = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum() + ""));
        orderEntity.setPayAmount(totalPrice);

        this.save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(totalPrice);
        orderItemEntity.setSkuQuantity(secKillOrderTo.getNum());

        orderItemService.save(orderItemEntity);


    }
}