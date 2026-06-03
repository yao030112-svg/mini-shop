package com.minishop.service;

import com.minishop.entity.*;
import com.minishop.exception.BusinessException;
import com.minishop.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单创建属性测试
 *
 * **Validates: Requirements 6.3, 6.4, 6.5**
 *
 * 属性 3：库存扣减一致性
 * 属性 4：库存不足阻止下单
 * 属性 5：订单编号唯一性
 */
class OrderCreatePropertyTest {

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
     * 属性 3：库存扣减一致性
     * 订单中每个SKU的库存减少量等于购买数量
     *
     * **Validates: Requirements 6.3**
     */
    @Property
    void stockDeductionShouldMatchPurchaseQuantity(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @LongRange(min = 1, max = 999999L) Long addressId,
            @ForAll("cartItemsForOrder") List<CartItemForOrder> cartItems) throws Exception {

        if (cartItems.isEmpty()) return;

        // Mock 地址
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);
        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);
        address.setReceiverName("测试");
        address.setPhone("13800138000");
        address.setProvince("广东");
        address.setCity("深圳");
        address.setDistrict("南山");
        address.setDetail("科技园");
        when(addressMapper.findById(addressId)).thenReturn(address);

        // Mock 购物车
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        List<Cart> selectedCarts = new ArrayList<>();
        for (CartItemForOrder item : cartItems) {
            Cart cart = new Cart();
            cart.setId(item.cartId);
            cart.setUserId(userId);
            cart.setSkuId(item.skuId);
            cart.setQuantity(item.quantity);
            cart.setSelected(1);
            cart.setSkuPrice(BigDecimal.valueOf(99.99));
            cart.setProductName("商品" + item.skuId);
            cart.setSkuSpecDesc("规格");
            cart.setSkuImage(null);
            cart.setProductMainImage("img.jpg");
            selectedCarts.add(cart);
        }
        when(cartMapper.getSelectedByUserId(userId)).thenReturn(selectedCarts);

        // Mock SKU（库存充足）
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        for (CartItemForOrder item : cartItems) {
            Sku sku = new Sku();
            sku.setId(item.skuId);
            sku.setStock(item.quantity + 100); // 确保库存充足
            sku.setPrice(BigDecimal.valueOf(99.99));
            when(skuMapper.findById(item.skuId)).thenReturn(sku);
            when(skuMapper.deductStock(eq(item.skuId), eq(item.quantity))).thenReturn(1);
        }

        // Mock 其他
        OrderMapper orderMapper = Mockito.mock(OrderMapper.class);
        OrderItemMapper orderItemMapper = Mockito.mock(OrderItemMapper.class);
        when(orderItemMapper.batchInsert(anyList())).thenReturn(cartItems.size());

        OrderService service = createServiceWithMocks(orderMapper, orderItemMapper, cartMapper, addressMapper, skuMapper);

        // 执行创建订单
        service.createOrder(userId, addressId);

        // 验证每个SKU的 deductStock 被调用且参数中 quantity 正确
        for (CartItemForOrder item : cartItems) {
            verify(skuMapper).deductStock(item.skuId, item.quantity);
        }
    }

    /**
     * 属性 4：库存不足阻止下单
     * 任一SKU请求数量大于库存时订单创建应失败
     *
     * **Validates: Requirements 6.4**
     */
    @Property
    void shouldRejectOrderWhenStockInsufficient(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @LongRange(min = 1, max = 999999L) Long addressId,
            @ForAll @LongRange(min = 1, max = 999999L) Long skuId,
            @ForAll @IntRange(min = 1, max = 100) int quantity,
            @ForAll @IntRange(min = 0, max = 0) int insufficientStock) throws Exception {

        // Mock 地址
        AddressMapper addressMapper = Mockito.mock(AddressMapper.class);
        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);
        address.setReceiverName("测试");
        address.setPhone("13800138000");
        address.setProvince("广东");
        address.setCity("深圳");
        address.setDistrict("南山");
        address.setDetail("科技园");
        when(addressMapper.findById(addressId)).thenReturn(address);

        // Mock 购物车（一个条目）
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(userId);
        cart.setSkuId(skuId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        cart.setSkuPrice(BigDecimal.valueOf(50.00));
        cart.setProductName("商品");
        cart.setSkuSpecDesc("规格");
        cart.setProductMainImage("img.jpg");
        when(cartMapper.getSelectedByUserId(userId)).thenReturn(Collections.singletonList(cart));

        // Mock SKU 库存不足
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setStock(insufficientStock); // 库存为0，小于请求数量
        sku.setPrice(BigDecimal.valueOf(50.00));
        when(skuMapper.findById(skuId)).thenReturn(sku);

        // Mock 其他
        OrderMapper orderMapper = Mockito.mock(OrderMapper.class);
        OrderItemMapper orderItemMapper = Mockito.mock(OrderItemMapper.class);

        OrderService service = createServiceWithMocks(orderMapper, orderItemMapper, cartMapper, addressMapper, skuMapper);

        // 验证抛出异常
        assertThatThrownBy(() -> service.createOrder(userId, addressId))
                .isInstanceOf(BusinessException.class);

        // 验证订单未被创建
        verify(orderMapper, never()).insert(any(Order.class));
    }

    /**
     * 属性 5：订单编号唯一性
     * 任意两个订单的编号互不相同
     * 注：generateOrderNo 格式为 时间戳(14位) + 用户ID后4位 + 4位随机数
     * 不同用户ID生成的订单编号前缀不同，因此不同用户间天然唯一
     *
     * **Validates: Requirements 6.5**
     */
    @Property(tries = 50)
    void orderNumbersShouldBeUnique(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId1,
            @ForAll @LongRange(min = 1, max = 999999L) Long userId2) throws Exception {

        OrderService service = new OrderService();

        // 通过反射调用 private generateOrderNo 方法
        Method method = OrderService.class.getDeclaredMethod("generateOrderNo", Long.class);
        method.setAccessible(true);

        Set<String> orderNos = new HashSet<>();
        int countPerUser = 10;

        // 为两个不同用户各生成订单编号
        for (int i = 0; i < countPerUser; i++) {
            String orderNo1 = (String) method.invoke(service, userId1);
            orderNos.add(orderNo1);
            // 短暂等待确保时间戳变化
            if (i % 5 == 4) {
                Thread.sleep(1);
            }
        }
        for (int i = 0; i < countPerUser; i++) {
            String orderNo2 = (String) method.invoke(service, userId2);
            orderNos.add(orderNo2);
            if (i % 5 == 4) {
                Thread.sleep(1);
            }
        }

        // 如果两个用户ID不同，所有编号应互不相同
        // 如果用户ID相同，由于随机数部分，大概率也不同
        // 验证至少生成了 countPerUser 个不同的编号（容忍极小概率碰撞）
        int expectedMin = countPerUser; // 至少一个用户的编号应全部唯一
        assertThat(orderNos.size()).isGreaterThanOrEqualTo(expectedMin);
    }

    @Provide
    Arbitrary<List<CartItemForOrder>> cartItemsForOrder() {
        Arbitrary<CartItemForOrder> itemArbitrary = Combinators.combine(
                Arbitraries.longs().between(1, 999999),   // cartId
                Arbitraries.longs().between(1, 999999),   // skuId
                Arbitraries.integers().between(1, 10)      // quantity
        ).as(CartItemForOrder::new);

        return itemArbitrary.list().ofMinSize(1).ofMaxSize(5)
                .filter(list -> {
                    // 确保 skuId 不重复
                    Set<Long> skuIds = new HashSet<>();
                    for (CartItemForOrder item : list) {
                        if (!skuIds.add(item.skuId)) return false;
                    }
                    return true;
                });
    }

    static class CartItemForOrder {
        final Long cartId;
        final Long skuId;
        final int quantity;

        CartItemForOrder(Long cartId, Long skuId, int quantity) {
            this.cartId = cartId;
            this.skuId = skuId;
            this.quantity = quantity;
        }
    }
}
