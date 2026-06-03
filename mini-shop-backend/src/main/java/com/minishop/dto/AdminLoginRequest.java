package com.minishop.dto;

import com.minishop.util.NoSpecialChars;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员登录请求 DTO
 */
@Data
public class AdminLoginRequest {

    /**
     * 管理员用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 32, message = "用户名长度必须在1-32之间")
    @NoSpecialChars
    private String username;

    /**
     * 管理员密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
