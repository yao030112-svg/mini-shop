package com.minishop.exception;

import lombok.Getter;

/**
 * 业务错误码常量
 */
@Getter
public enum ErrorCode {

    UNAUTHORIZED(401, "未认证/Token过期"),
    FORBIDDEN(403, "权限不足"),
    STOCK_NOT_ENOUGH(1001, "库存不足"),
    PRODUCT_OFF_SHELF(1002, "商品已下架"),
    ADDRESS_LIMIT_EXCEEDED(1003, "地址数量超限"),
    CATEGORY_HAS_PRODUCTS(1004, "分类下存在商品，无法删除"),
    USER_DISABLED(1005, "用户已被禁用");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
