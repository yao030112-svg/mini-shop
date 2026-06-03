package com.minishop.controller;

import com.minishop.dto.CartAddRequest;
import com.minishop.dto.CartUpdateRequest;
import com.minishop.entity.Cart;
import com.minishop.security.UserContext;
import com.minishop.service.CartService;
import com.minishop.vo.CartListVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器（需认证）
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表（含已选商品总价）
     */
    @GetMapping("/list")
    public Result<CartListVO> list() {
        Long userId = UserContext.getCurrentUserId();
        List<Cart> cartList = cartService.getCartList(userId);
        BigDecimal totalPrice = cartService.calculateTotalPrice(userId);

        CartListVO vo = new CartListVO();
        vo.setCartList(cartList);
        vo.setTotalPrice(totalPrice);
        return Result.success(vo);
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartAddRequest request) {
        Long userId = UserContext.getCurrentUserId();
        cartService.addToCart(userId, request.getSkuId());
        return Result.success();
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        cartService.updateQuantity(request.getId(), request.getQuantity(), userId);
        return Result.success();
    }

    /**
     * 删除购物车条目
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        cartService.deleteItem(id, userId);
        return Result.success();
    }

    /**
     * 切换单个条目选中状态
     */
    @PutMapping("/select/{id}")
    public Result<Void> toggleSelect(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Long userId = UserContext.getCurrentUserId();
        Boolean selected = request.get("selected");
        if (selected == null) {
            selected = true;
        }
        cartService.toggleSelect(id, selected, userId);
        return Result.success();
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/select-all")
    public Result<Void> selectAll(@RequestBody Map<String, Boolean> request) {
        Long userId = UserContext.getCurrentUserId();
        Boolean selected = request.get("selected");
        if (selected == null) {
            selected = true;
        }
        cartService.selectAll(userId, selected);
        return Result.success();
    }
}
