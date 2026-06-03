package com.minishop.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 更新购物车数量请求DTO
 */
@Data
public class CartUpdateRequest {

    /** 购物车条目ID */
    @NotNull(message = "购物车条目ID不能为空")
    private Long id;

    /** 新数量 */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量最小为1")
    private Integer quantity;
}
