package com.minishop.service;

import com.minishop.entity.Order;
import com.minishop.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 支付状态转换属性测试
 *
 * **Validates: Requirements 7.2, 7.3**
 *
 * 属性 7：支付状态转换正确性 - 待支付订单支付成功后状态变为已支付
 */
class PaymentPropertyTest {

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
     * 属性 7：支付状态转换正确性
     * 待支付订单支付成功后状态变为已支付（status 从 0 变为 1）
     *
     * **Validates: Requirements 7.2**
     */
    @Property
    void payOrderShouldChangeStatusFromPendingToPaid(
            @ForAll @LongRange(min = 1, max = 999999L) Long orderId,
            @ForAll @LongRange(min = 1, max = 999999L) Long userId) throws Exception {

        // Mock orderMapper.findById 返回 status=0 的订单
        OrderMapper orderMapper = Mockito.mock(OrderMapper.class);
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderNo("20240101120000000100001");
        order.setTotalAmount(BigDecimal.valueOf(199.99));
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now().minusMinutes(5));
        when(orderMapper.findById(orderId)).thenReturn(order);
        when(orderMapper.updateStatus(eq(orderId), eq(1))).thenReturn(1);
        when(orderMapper.updatePaymentTime(eq(orderId), any(LocalDateTime.class))).thenReturn(1);

        // Mock 其他依赖
        OrderItemMapper orderItemMapper = Mockito.mock(OrderItemMapper.class);
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);

        OrderService service = createServiceWithMocks(orderMapper, orderItemMapper, cartMapper, addressMapper, skuMapper);

        // 调用 payOrder
        Order result = service.payOrder(orderId, userId);

        // 验证 updateStatus 被调用且参数为 (orderId, 1)
        verify(orderMapper).updateStatus(orderId, 1);

        // 验证返回的订单状态为已支付
        assertThat(result.getStatus()).isEqualTo(1);

        // 验证支付时间被记录
        verify(orderMapper).updatePaymentTime(eq(orderId), any(LocalDateTime.class));
        assertThat(result.getPaymentTime()).isNotNull();
    }
}
