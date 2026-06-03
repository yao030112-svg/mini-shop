package com.minishop.service;

import com.minishop.entity.Order;
import com.minishop.entity.OrderItem;
import com.minishop.mapper.*;
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
 * 退款恢复库存属性测试
 *
 * **Validates: Requirements 11.3**
 *
 * 属性 14：退款恢复库存 - 退款后SKU库存增加购买数量
 */
class RefundPropertyTest {

    private OrderService createServiceWithMocks(
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            CartMapper cartMapper,
            AddressMapper addressMapper,
            SkuMapper skuMapper) throws Exception {

        OrderService service = new OrderService();

        setField(service, "orderMapper", orderMapper);
        setField(service, "orderItemMapper", orderItemMapper);
        setField(service, "cartMapper", cartMapper);
        setField(service, "addressMapper", addressMapper);
        setField(service, "skuMapper", skuMapper);

        return service;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * 属性 14：退款恢复库存
     * 退款后每个SKU的 restoreStock 被调用且参数为 (skuId, quantity)
     *
     * **Validates: Requirements 11.3**
     */
    @Property
    void refundOrderShouldRestoreStock(
            @ForAll @LongRange(min = 1, max = 999999L) Long orderId,
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll("orderItemsProvider") List<RefundItemData> items) throws Exception {

        if (items.isEmpty()) return;

        // Mock 订单
        OrderMapper orderMapper = Mockito.mock(OrderMapper.class);
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderNo("20240101120000000100001");
        order.setTotalAmount(BigDecimal.valueOf(500));
        order.setStatus(1); // 已支付
        order.setCreateTime(LocalDateTime.now().minusDays(1));
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(orderMapper.updateStatus(orderId, 5)).thenReturn(1);

        // Mock 订单项
        OrderItemMapper orderItemMapper = Mockito.mock(OrderItemMapper.class);
        List<OrderItem> orderItems = new ArrayList<>();
        for (RefundItemData itemData : items) {
            OrderItem item = new OrderItem();
            item.setId(itemData.itemId);
            item.setOrderId(orderId);
            item.setSkuId(itemData.skuId);
            item.setQuantity(itemData.quantity);
            item.setProductName("商品" + itemData.skuId);
            item.setPrice(BigDecimal.valueOf(100));
            orderItems.add(item);
        }
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(orderItems);

        // Mock SkuMapper
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        when(skuMapper.restoreStock(anyLong(), anyInt())).thenReturn(1);

        // Mock 其他
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);

        OrderService service = createServiceWithMocks(orderMapper, orderItemMapper, cartMapper, addressMapper, skuMapper);

        // 调用退款
        service.refundOrder(orderId);

        // 验证订单状态更新为已退款
        verify(orderMapper).updateStatus(orderId, 5);

        // 验证每个SKU的 restoreStock 被调用且参数为 (skuId, quantity)
        for (RefundItemData itemData : items) {
            verify(skuMapper).restoreStock(itemData.skuId, itemData.quantity);
        }
    }

    @Provide
    Arbitrary<List<RefundItemData>> orderItemsProvider() {
        Arbitrary<RefundItemData> itemArbitrary = Combinators.combine(
                Arbitraries.longs().between(1, 999999),   // itemId
                Arbitraries.longs().between(1, 999999),   // skuId
                Arbitraries.integers().between(1, 20)      // quantity
        ).as(RefundItemData::new);

        return itemArbitrary.list().ofMinSize(1).ofMaxSize(5)
                .filter(list -> {
                    // 确保 skuId 不重复
                    Set<Long> skuIds = new HashSet<>();
                    for (RefundItemData item : list) {
                        if (!skuIds.add(item.skuId)) return false;
                    }
                    return true;
                });
    }

    static class RefundItemData {
        final Long itemId;
        final Long skuId;
        final int quantity;

        RefundItemData(Long itemId, Long skuId, int quantity) {
            this.itemId = itemId;
            this.skuId = skuId;
            this.quantity = quantity;
        }
    }
}
