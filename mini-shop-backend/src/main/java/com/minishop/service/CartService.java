package com.minishop.service;

import com.minishop.entity.Cart;
import com.minishop.entity.Sku;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.CartMapper;
import com.minishop.mapper.SkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车业务逻辑层
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 添加到购物车
     * 如果相同用户+相同SKU已存在，则数量+1
     */
    public void addToCart(Long userId, Long skuId) {
        // 校验SKU是否存在
        Sku sku = skuMapper.findById(skuId);
        if (sku == null) {
            throw new BusinessException(400, "SKU不存在");
        }

        // 校验库存
        if (sku.getStock() <= 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        // 查找是否已存在相同SKU的购物车条目
        Cart existing = cartMapper.findByUserIdAndSkuId(userId, skuId);
        if (existing != null) {
            // 已存在，数量+1（不超过库存）
            int newQuantity = existing.getQuantity() + 1;
            if (newQuantity > sku.getStock()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            cartMapper.updateQuantity(existing.getId(), newQuantity);
        } else {
            // 不存在，新增条目
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setSkuId(skuId);
            cart.setQuantity(1);
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
    }

    /**
     * 更新购物车数量
     * 校验数量范围：[1, 该SKU当前库存]
     */
    public void updateQuantity(Long id, Integer quantity, Long userId) {
        // 查找购物车条目
        Cart cart = cartMapper.findById(id);
        if (cart == null) {
            throw new BusinessException(400, "购物车条目不存在");
        }

        // 校验是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        // 校验数量最小值
        if (quantity < 1) {
            throw new BusinessException(400, "数量不能小于1");
        }

        // 校验数量最大值（不超过库存）
        Sku sku = skuMapper.findById(cart.getSkuId());
        if (sku == null) {
            throw new BusinessException(400, "SKU不存在");
        }
        if (quantity > sku.getStock()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        cartMapper.updateQuantity(id, quantity);
    }

    /**
     * 删除购物车条目
     */
    public void deleteItem(Long id, Long userId) {
        // 查找购物车条目
        Cart cart = cartMapper.findById(id);
        if (cart == null) {
            throw new BusinessException(400, "购物车条目不存在");
        }

        // 校验是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        cartMapper.deleteById(id);
    }

    /**
     * 切换单个条目选中状态
     */
    public void toggleSelect(Long id, Boolean selected, Long userId) {
        Cart cart = cartMapper.findById(id);
        if (cart == null) {
            throw new BusinessException(400, "购物车条目不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        cartMapper.updateSelected(id, selected ? 1 : 0);
    }

    /**
     * 全选/取消全选
     */
    public void selectAll(Long userId, Boolean selected) {
        cartMapper.updateSelectAll(userId, selected ? 1 : 0);
    }

    /**
     * 获取购物车列表（含SKU和商品信息）
     */
    public List<Cart> getCartList(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 计算选中商品总价
     */
    public BigDecimal calculateTotalPrice(Long userId) {
        List<Cart> selectedItems = cartMapper.getSelectedByUserId(userId);
        return selectedItems.stream()
                .map(item -> item.getSkuPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
