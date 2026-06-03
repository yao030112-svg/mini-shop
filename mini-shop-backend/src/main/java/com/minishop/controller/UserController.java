package com.minishop.controller;

import com.minishop.entity.User;
import com.minishop.security.UserContext;
import com.minishop.service.UserService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     * 依赖 JwtInterceptor 已经解析并设置用户信息到 ThreadLocal
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        try {
            // 从 ThreadLocal 获取用户 ID
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 查询用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            // 不返回敏感信息
            user.setPassword(null);
            user.setOpenid(null);

            return Result.success(user);
        } catch (Exception e) {
            return Result.error(500, "系统内部错误");
        }
    }
}
