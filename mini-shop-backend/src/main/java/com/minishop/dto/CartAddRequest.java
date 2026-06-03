package com.minishop.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 添加购物车请求DTO
 */
@Data
public class CartAddRequest {

    /** SKU ID */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;
}
