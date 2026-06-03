package com.minishop.security;

/**
 * 用户上下文，使用 ThreadLocal 存储当前请求的用户信息
 * 在拦截器中设置，在请求结束后清除
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(Long userId, Integer role) {
        CURRENT_USER.set(new UserInfo(userId, role));
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserInfo userInfo = CURRENT_USER.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getCurrentRole() {
        UserInfo userInfo = CURRENT_USER.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 清除当前用户信息（防止内存泄漏）
     */
    public static void clear() {
        CURRENT_USER.remove();
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        private final Long userId;
        private final Integer role;

        public UserInfo(Long userId, Integer role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public Integer getRole() {
            return role;
        }
    }
}
