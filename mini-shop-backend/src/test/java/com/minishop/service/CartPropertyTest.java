package com.minishop.service;

import com.minishop.entity.Cart;
import com.minishop.entity.Sku;
import com.minishop.exception.BusinessException;
import com.minishop.mapper.CartMapper;
import com.minishop.mapper.SkuMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 购物车属性测试
 *
 * **Validates: Requirements 5.6, 5.2, 5.3**
 *
 * 属性 1：购物车总价计算一致性
 * 属性 2：购物车添加相同SKU的幂等合并
 * 属性 16：购物车数量范围约束
 */
class CartPropertyTest {

    private CartService createCartServiceWithMocks(CartMapper cartMapper, SkuMapper skuMapper) throws Exception {
        CartService cartService = new CartService();

        Field cartMapperField = CartService.class.getDeclaredField("cartMapper");
        cartMapperField.setAccessible(true);
        cartMapperField.set(cartService, cartMapper);

        Field skuMapperField = CartService.class.getDeclaredField("skuMapper");
        skuMapperField.setAccessible(true);
        skuMapperField.set(cartService, skuMapper);

        return cartService;
    }

    /**
     * 属性 1：购物车总价计算一致性
     * 总价 = Σ(每个选中条目的 SKU单价 × 数量)
     *
     * **Validates: Requirements 5.6**
     */
    @Property
    void totalPriceShouldEqualSumOfSelectedItemPrices(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll("cartItemsProvider") List<CartItemData> items) throws Exception {

        // 构造 Mock 返回的选中购物车条目
        List<Cart> selectedCarts = new ArrayList<>();
        BigDecimal expectedTotal = BigDecimal.ZERO;

        for (CartItemData item : items) {
            Cart cart = new Cart();
            cart.setId(item.cartId);
            cart.setUserId(userId);
            cart.setSkuId(item.skuId);
            cart.setQuantity(item.quantity);
            cart.setSelected(1);
            cart.setSkuPrice(item.price);
            selectedCarts.add(cart);

            expectedTotal = expectedTotal.add(
                    item.price.multiply(BigDecimal.valueOf(item.quantity))
            );
        }

        // Mock CartMapper
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        when(cartMapper.getSelectedByUserId(userId)).thenReturn(selectedCarts);

        CartService cartService = createCartServiceWithMocks(cartMapper, skuMapper);

        // 执行计算
        BigDecimal actualTotal = cartService.calculateTotalPrice(userId);

        // 验证总价一致性
        assertThat(actualTotal).isEqualByComparingTo(expectedTotal);
    }

    @Provide
    Arbitrary<List<CartItemData>> cartItemsProvider() {
        Arbitrary<CartItemData> itemArbitrary = Combinators.combine(
                Arbitraries.longs().between(1, 999999),   // cartId
                Arbitraries.longs().between(1, 999999),   // skuId
                Arbitraries.integers().between(1, 99),     // quantity
                Arbitraries.bigDecimals()
                        .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(9999.99))
                        .ofScale(2)                        // price
        ).as(CartItemData::new);

        return itemArbitrary.list().ofMinSize(0).ofMaxSize(10);
    }

    /**
     * 属性 2：购物车添加相同SKU的幂等合并
     * 相同SKU再次添加后条目数量仍为1，数量字段增加1
     *
     * **Validates: Requirements 5.2**
     */
    @Property
    void addingSameSkuShouldMergeQuantity(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @LongRange(min = 1, max = 999999L) Long skuId,
            @ForAll @IntRange(min = 1, max = 98) int existingQuantity) throws Exception {

        // Mock SKU 存在且有足够库存
        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setStock(existingQuantity + 10); // 确保库存充足
        sku.setPrice(BigDecimal.valueOf(99.99));
        when(skuMapper.findById(skuId)).thenReturn(sku);

        // Mock 已存在的购物车条目
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        Cart existingCart = new Cart();
        existingCart.setId(100L);
        existingCart.setUserId(userId);
        existingCart.setSkuId(skuId);
        existingCart.setQuantity(existingQuantity);
        existingCart.setSelected(1);
        when(cartMapper.findByUserIdAndSkuId(userId, skuId)).thenReturn(existingCart);

        CartService cartService = createCartServiceWithMocks(cartMapper, skuMapper);

        // 执行添加操作
        cartService.addToCart(userId, skuId);

        // 验证调用了 updateQuantity 而非 insert（幂等合并）
        verify(cartMapper).updateQuantity(existingCart.getId(), existingQuantity + 1);
        verify(cartMapper, never()).insert(any(Cart.class));
    }

    /**
     * 属性 16：购物车数量范围约束
     * 修改后的数量应在 [1, 该SKU当前库存] 范围内
     * 当数量 < 1 或 > 库存时应抛出异常
     *
     * **Validates: Requirements 5.3**
     */
    @Property
    void quantityBelowMinimumShouldBeRejected(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @LongRange(min = 1, max = 999999L) Long cartId,
            @ForAll @LongRange(min = 1, max = 999999L) Long skuId,
            @ForAll @IntRange(min = -100, max = 0) int invalidQuantity) throws Exception {

        // Mock 购物车条目存在
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setSkuId(skuId);
        cart.setQuantity(5);
        when(cartMapper.findById(cartId)).thenReturn(cart);

        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setStock(100);
        when(skuMapper.findById(skuId)).thenReturn(sku);

        CartService cartService = createCartServiceWithMocks(cartMapper, skuMapper);

        // 验证数量小于1时抛出异常
        assertThatThrownBy(() -> cartService.updateQuantity(cartId, invalidQuantity, userId))
                .isInstanceOf(BusinessException.class);

        // 验证 updateQuantity 未被调用（数据保持不变）
        verify(cartMapper, never()).updateQuantity(anyLong(), anyInt());
    }

    /**
     * 属性 16（补充）：购物车数量超过库存应被拒绝
     *
     * **Validates: Requirements 5.3**
     */
    @Property
    void quantityAboveStockShouldBeRejected(
            @ForAll @LongRange(min = 1, max = 999999L) Long userId,
            @ForAll @LongRange(min = 1, max = 999999L) Long cartId,
            @ForAll @LongRange(min = 1, max = 999999L) Long skuId,
            @ForAll @IntRange(min = 1, max = 100) int stock) throws Exception {

        int exceedingQuantity = stock + 1; // 超过库存的数量

        // Mock 购物车条目存在
        CartMapper cartMapper = Mockito.mock(CartMapper.class);
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setSkuId(skuId);
        cart.setQuantity(1);
        when(cartMapper.findById(cartId)).thenReturn(cart);

        SkuMapper skuMapper = Mockito.mock(SkuMapper.class);
        Sku sku = new Sku();
        sku.setId(skuId);
        sku.setStock(stock);
        when(skuMapper.findById(skuId)).thenReturn(sku);

        CartService cartService = createCartServiceWithMocks(cartMapper, skuMapper);

        // 验证数量超过库存时抛出异常
        assertThatThrownBy(() -> cartService.updateQuantity(cartId, exceedingQuantity, userId))
                .isInstanceOf(BusinessException.class);

        // 验证 updateQuantity 未被调用（数据保持不变）
        verify(cartMapper, never()).updateQuantity(anyLong(), anyInt());
    }

    /**
     * 辅助数据类：购物车条目数据
     */
    static class CartItemData {
        final Long cartId;
        final Long skuId;
        final int quantity;
        final BigDecimal price;

        CartItemData(Long cartId, Long skuId, int quantity, BigDecimal price) {
            this.cartId = cartId;
            this.skuId = skuId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
