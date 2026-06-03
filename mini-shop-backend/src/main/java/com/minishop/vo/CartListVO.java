package com.minishop.vo;

import com.minishop.entity.Cart;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车列表响应 VO
 * 包含购物车条目列表和已选商品总价
 */
@Data
public class CartListVO {

    /**
     * 购物车条目列表
     */
    private List<Cart> cartList;

    /**
     * 已选商品总价
     */
    private BigDecimal totalPrice;
}
