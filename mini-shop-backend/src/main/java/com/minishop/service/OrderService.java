package com.minishop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minishop.entity.*;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.*;
import com.minishop.vo.OrderDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private SkuMapper skuMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建订单
     *
     * @param userId    用户ID
     * @param addressId 收货地址ID
     * @return 创建的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long addressId) {
        // 1. 验证收货地址存在且属于当前用户
        Address address = addressMapper.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(400, "收货地址不存在");
        }

        // 2. 获取购物车选中条目
        List<Cart> selectedItems = cartMapper.getSelectedByUserId(userId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            throw new BusinessException(400, "购物车中没有选中的商品");
        }

        // 3. 检查每个SKU库存并扣减库存（乐观锁）
        for (Cart cartItem : selectedItems) {
            Sku sku = skuMapper.findById(cartItem.getSkuId());
            if (sku == null) {
                throw new BusinessException(400, "商品SKU不存在");
            }
            if (sku.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            // 4. 使用乐观锁扣减库存
            int rows = skuMapper.deductStock(cartItem.getSkuId(), cartItem.getQuantity());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
        }

        // 5. 生成唯一订单编号
        String orderNo = generateOrderNo(userId);

        // 6. 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : selectedItems) {
            BigDecimal itemTotal = cartItem.getSkuPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 7. 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付
        order.setAddressSnapshot(buildAddressSnapshot(address));
        orderMapper.insert(order);

        // 8. 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cartItem : selectedItems) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(cartItem.getSkuId());
            item.setProductName(cartItem.getProductName());
            item.setSkuSpecDesc(cartItem.getSkuSpecDesc());
            item.setPrice(cartItem.getSkuPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setImage(cartItem.getSkuImage() != null ? cartItem.getSkuImage() : cartItem.getProductMainImage());
            orderItems.add(item);
        }
        orderItemMapper.batchInsert(orderItems);

        // 9. 清除已下单的购物车条目
        List<Long> cartIds = selectedItems.stream()
                .map(Cart::getId)
                .collect(Collectors.toList());
        cartMapper.deleteByIds(cartIds);

        return order;
    }

    /**
     * 生成唯一订单编号：时间戳 + 用户ID后4位 + 4位随机数
     */
    private String generateOrderNo(Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String userSuffix = String.format("%04d", userId % 10000);
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return timestamp + userSuffix + random;
    }

    /**
     * 构建地址快照JSON
     */
    private String buildAddressSnapshot(Address address) {
        try {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("receiverName", address.getReceiverName());
            snapshot.put("phone", address.getPhone());
            snapshot.put("province", address.getProvince());
            snapshot.put("city", address.getCity());
            snapshot.put("district", address.getDistrict());
            snapshot.put("detail", address.getDetail());
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BusinessException(500, "地址序列化失败");
        }
    }

    /**
     * 获取订单列表（按时间倒序，支持状态筛选）
     *
     * @param userId 用户ID
     * @param status 订单状态（null时查全部）
     * @return 订单列表
     */
    public List<Order> getOrderList(Long userId, Integer status) {
        return orderMapper.selectByUserIdAndStatus(userId, status);
    }

    /**
     * 获取订单详情（含订单项列表）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 订单详情VO
     */
    public OrderDetailVO getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看此订单");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return new OrderDetailVO(order, items);
    }

    /**
     * 取消订单（只有待支付状态可取消，取消后恢复库存）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "只有待支付订单可以取消");
        }

        // 更新订单状态为已取消
        orderMapper.updateStatus(orderId, 4);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            skuMapper.restoreStock(item.getSkuId(), item.getQuantity());
        }
    }

    /**
     * 管理员获取订单列表（支持 status、userId 筛选）
     *
     * @param status 订单状态（null时查全部）
     * @param userId 用户ID（null时查全部用户）
     * @return 订单列表
     */
    public List<Order> getAdminOrderList(Integer status, Long userId) {
        return orderMapper.selectAdminList(status, userId);
    }

    /**
     * 管理员获取订单详情（不需要验证用户权限）
     *
     * @param orderId 订单ID
     * @return 订单详情VO
     */
    public OrderDetailVO getOrderDetailForAdmin(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return new OrderDetailVO(order, items);
    }

    /**
     * 发货（验证状态为已支付 status=1，更新为已发货 status=2，记录发货时间）
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(400, "只有已支付订单才能发货");
        }
        orderMapper.updateStatus(orderId, 2);
        orderMapper.updateShippingTime(orderId, LocalDateTime.now());
    }

    /**
     * 退款（更新状态为已退款 status=5，恢复库存）
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        // 更新订单状态为已退款
        orderMapper.updateStatus(orderId, 5);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            skuMapper.restoreStock(item.getSkuId(), item.getQuantity());
        }
    }

    /**
     * 模拟支付订单
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 支付后的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order payOrder(Long orderId, Long userId) {
        // 1. 验证订单存在
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }

        // 2. 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        // 3. 验证订单状态为待支付
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        // 4. 模拟支付（直接模拟成功）
        LocalDateTime paymentTime = LocalDateTime.now();
        orderMapper.updateStatus(orderId, 1); // 更新状态为已支付
        orderMapper.updatePaymentTime(orderId, paymentTime); // 记录支付时间

        // 5. 返回更新后的订单
        order.setStatus(1);
        order.setPaymentTime(paymentTime);
        return order;
    }
}
