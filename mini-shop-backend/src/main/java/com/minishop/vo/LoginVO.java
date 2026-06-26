package com.minishop.vo;

import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer role;
}
