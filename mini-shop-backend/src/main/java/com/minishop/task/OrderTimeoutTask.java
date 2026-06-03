package com.minishop.task;

import com.minishop.entity.Order;
import com.minishop.entity.OrderItem;
import com.minishop.mapper.OrderItemMapper;
import com.minishop.mapper.OrderMapper;
import com.minishop.mapper.SkuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时取消定时任务
 * 每分钟扫描超过30分钟未支付的订单，自动取消并恢复库存
 */
@Component
public class OrderTimeoutTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTimeoutTask.class);

    /** 超时时间：30分钟 */
    private static final int TIMEOUT_MINUTES = 30;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 每分钟执行一次，扫描并取消超时未支付的订单
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(deadline);

        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }

        logger.info("发现 {} 个超时未支付订单，开始取消处理", timeoutOrders.size());

        for (Order order : timeoutOrders) {
            try {
                cancelOrder(order);
                logger.info("订单 {} 已超时取消", order.getOrderNo());
            } catch (Exception e) {
                logger.error("取消超时订单 {} 失败: {}", order.getOrderNo(), e.getMessage(), e);
            }
        }
    }

    /**
     * 取消单个超时订单并恢复库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Order order) {
        // 1. 更新订单状态为已取消(status=4)
        orderMapper.updateStatus(order.getId(), 4);

        // 2. 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());

        // 3. 恢复每个SKU的库存
        for (OrderItem item : orderItems) {
            skuMapper.restoreStock(item.getSkuId(), item.getQuantity());
        }
    }
}
