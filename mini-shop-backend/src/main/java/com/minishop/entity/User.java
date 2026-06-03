package com.minishop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

    /**
     * 角色：0普通用户 1管理员
     */
    private Integer role;

    /**
     * 管理员密码(加密)
     */
    private String password;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删除 1已删除
     */
    private Integer deleted;
}
