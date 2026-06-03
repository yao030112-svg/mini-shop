package com.minishop.service;

import com.minishop.entity.Order;
import com.minishop.entity.OrderItem;
import com.minishop.mapper.OrderItemMapper;
import com.minishop.mapper.OrderMapper;
import com.minishop.mapper.SkuMapper;
import com.minishop.task.OrderTimeoutTask;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单超时取消属性测试
 *
 * **Validates: Requirements 7.4**
 *
 * 属性 6：订单超时取消恢复库存 - 取消后SKU库存恢复
 */
class OrderTimeoutPropertyTest {

    private OrderTimeoutTask createTaskWithMocks(
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            SkuMapper skuMapper) throws Exception {

        OrderTimeoutTask task = new OrderTimeoutTask();

        setField(task, "orderMapper", orderMapper);
        setField(task, "orderItemMapper", orderItemMapper);
        setField(task, "skuMapper", skuMapper);

        return task;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * 属性 6：订单超时取消恢复库存
     * 取消后每个SKU的 restoreStock 被调用且参数为 (skuId, quantity)
     *
     * **Validates: Requirements 7.4**
     */
    @Property
    void cancelTimeoutOrderShouldRestoreStock(
            @ForAll @LongRange(min = 1, max = 999999L) Long orderId,
            @ForAll("orderItemsProvider") List<OrderItemData> items) throws Exception {

        if (items.isEmpty()) return;

        // 构造超时订单
        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo("20240101120000000100001");
        order.setUserId(1L);
        order.setStatus(0); // 待支付
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setCreateTime(LocalDateTime.now().minusMinutes(35)); // 超时

        // Mock OrderMapper
        OrderMapper orderMapper = Mockito.mock(OrderMapper.class);
        when(orderMapper.updateStatus(orderId, 4)).thenReturn(1);

        // Mock OrderItemMapper
        OrderItemMapper orderItemMapper = Mockito.mock(OrderItemMapper.class);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemData itemData : items) {
            OrderItem item = new OrderItem();
            item.setId(itemData.itemId);
            item.setOrderId(orderId);
            item.setSkuId(itemData.skuId);
            item.setQuantity(itemData.quantity);
            item.setProductName("商品" + itemData.skuId);
            item.setPrice(BigDecimal.valueOf(50));
            orderItems.add(item);
        }
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(orderItems);

        // Mock SkuMapper
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        when(skuMapper.restoreStock(anyLong(), anyInt())).thenReturn(1);

        OrderTimeoutTask task = createTaskWithMocks(orderMapper, orderItemMapper, skuMapper);

        // 调用 cancelOrder
        task.cancelOrder(order);

        // 验证订单状态更新为已取消
        verify(orderMapper).updateStatus(orderId, 4);

        // 验证每个SKU的 restoreStock 被调用且参数正确
        for (OrderItemData itemData : items) {
            verify(skuMapper).restoreStock(itemData.skuId, itemData.quantity);
        }
    }

    @Provide
    Arbitrary<List<OrderItemData>> orderItemsProvider() {
        Arbitrary<OrderItemData> itemArbitrary = Combinators.combine(
                Arbitraries.longs().between(1, 999999),   // itemId
                Arbitraries.longs().between(1, 999999),   // skuId
                Arbitraries.integers().between(1, 20)      // quantity
        ).as(OrderItemData::new);

        return itemArbitrary.list().ofMinSize(1).ofMaxSize(5)
                .filter(list -> {
                    // 确保 skuId 不重复
                    Set<Long> skuIds = new HashSet<>();
                    for (OrderItemData item : list) {
                        if (!skuIds.add(item.skuId)) return false;
                    }
                    return true;
                });
    }

    static class OrderItemData {
        final Long itemId;
        final Long skuId;
        final int quantity;

        OrderItemData(Long itemId, Long skuId, int quantity) {
            this.itemId = itemId;
            this.skuId = skuId;
            this.quantity = quantity;
        }
    }
}
