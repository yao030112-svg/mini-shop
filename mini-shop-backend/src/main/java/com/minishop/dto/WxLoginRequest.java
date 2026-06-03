package com.minishop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求 DTO
 */
@Data
public class WxLoginRequest {

    /**
     * 微信登录 code
     */
    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
