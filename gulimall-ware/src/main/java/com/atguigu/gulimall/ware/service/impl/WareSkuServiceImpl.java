package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 条件分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 将成功采购项的进行入库
     *
     * @param skuId  商品skuid
     * @param wareId 仓库id
     * @param skuNum 采购数量
     */
    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录  新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum); // 更新库存信息
        }

    }

    /**
     * 查询sku是否有库存
     *
     * @param skuIds 要查询的skuId集合
     * @return
     */
    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {

            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 锁定某个订单的库存
     * <p>
     * 库存解锁场景：
     * 1)、下单成功，订单过期没有支付被系统自动取消、被用户手动取消
     * <p>
     * 2)、下订单成功，库存锁定成功，但是后面的业务调用失败了，导致订单回滚，那么之前锁定的库存就要解锁
     *
     * @param wareSkuLockVo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLocKStock(WareSkuLockVo wareSkuLockVo) {

        // TODO 按照下单的地址，找到就近仓库，锁定库存

        /**
         * 保存库存工作单的详情
         * 事务回滚追溯数据
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);


        // 找到每个商品在哪些仓库有库存
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();

            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);

            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareIds(wareIds);

            skuWareHasStock.setNum(item.getCount());

            return skuWareHasStock;

        }).collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {

            Boolean skuStocked = false;

            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();
            if (wareIds == null && wareIds.size() <= 0) {
                // 所有仓库都没有该商品的库存
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) { // 遍历每个仓库，直到有一个仓库锁定库存成功
                Integer count = baseMapper.lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    /**
                     * 保存库存工作单
                     */
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", skuWareHasStock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    // 告诉RabbitMQ库存锁定成功
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setStockDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                    break;
                } else {
                    // 当前仓库锁失败 尝试下一个仓库
                }
            }

            if (skuStocked == false) {
                // 当前商品所有仓库都锁库存失败了  所有商品也不需要继续执行锁库存了
                throw new NoStockException(skuId);
            }
        }

        // 全部锁定成功
        return true;
    }

    /**
     * 构建解锁库存内容
     *
     * @param stockLockedTo
     */
    @Transactional
    @Override
    public void buidUnlockStock(StockLockedTo stockLockedTo) {

        StockDetailTo stockDetail = stockLockedTo.getStockDetail();

        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(stockDetail.getId());
        if (taskDetailEntity != null) {

            // 调用远程服务，查询订单的信息
            Long wareOrderTaskId = stockLockedTo.getId(); // 库存工作单的id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(wareOrderTaskId);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderByOrderSn(orderSn);
            if (r.getCode() == 0) {
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });

                // 判断订单状态
                // // 订单不存在，订单回滚了，但是库存锁了
                if (orderVo == null || orderVo.getStatus() == 4) { // 订单被取消了

                    if (taskDetailEntity.getLockStatus() == 1) {
                        // 解锁库存
                        unLockStock(stockDetail.getSkuId(), stockDetail.getWareId(), stockDetail.getSkuNum(), taskDetailEntity.getId());
                    } else {
                        log.info("******库存已被解锁了，本次不再执行解锁操作******");
                    }

                }
            } else {
                // 远程调用解锁库存失败，消息拒绝，将消息重新放回队列，让别人继续消费解锁
                throw new RuntimeException("远程服务解锁库存失败！");
            }

        } else {
            // 无需解锁
        }
    }


    /**
     * 订单关闭解锁库存
     * 防止订单服务卡顿，导致订单状态一直改不了，库存信息优先到达时查询订单状态还是新建状态，所以导致库存无法解锁，引起卡顿的订单永远无法解锁
     * @param orderTo
     */
    @Transactional
    @Override
    public void buidUnlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 根据订单号查询库存工作单信息
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getByOrderSn(orderSn);
        Long taskEntityId = taskEntity.getId();
        // 按照工作单的id查询未解锁的库存
        List<WareOrderTaskDetailEntity> orderTaskDetailEntityList = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskEntityId)
                        .eq("lock_status", 1));

        for (WareOrderTaskDetailEntity entity : orderTaskDetailEntityList) {
            // 依次遍历解锁
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }

    }

    /**
     * 解锁库存
     *
     * @param skuId        商品skuId
     * @param wareId       仓库id
     * @param count        需要解锁的数量
     * @param taskDetailId 库存工作单id
     */
    public void unLockStock(Long skuId, Long wareId, Integer count, Long taskDetailId) {
        // 1、解锁库存
        baseMapper.unLockStock(skuId, wareId, count);
        // 2、更新库存工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        taskDetailEntity.setLockStatus(2); // 已解锁
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }

    /* ================================================================================================================================================= */



    @Data
    class SkuWareHasStock {

        private Long skuId;

        private Integer num;

        private List<Long> wareIds;
    }
}