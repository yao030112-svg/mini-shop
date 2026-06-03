package com.minishop.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建订单请求DTO
 */
@Data
public class OrderCreateRequest {

    /** 收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
}
