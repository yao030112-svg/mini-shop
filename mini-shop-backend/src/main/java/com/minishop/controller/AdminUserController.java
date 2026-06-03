package com.minishop.controller;

import com.minishop.entity.User;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.security.UserContext;
import com.minishop.service.UserService;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 禁用用户
     */
    @PutMapping("/disable/{id}")
    public Result<Void> disableUser(@PathVariable Long id) {
        checkAdmin();
        userService.disableUser(id);
        return Result.success();
    }

    /**
     * 启用用户
     */
    @PutMapping("/enable/{id}")
    public Result<Void> enableUser(@PathVariable Long id) {
        checkAdmin();
        userService.enableUser(id);
        return Result.success();
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> getUserList() {
        checkAdmin();
        List<User> userList = userService.getUserList();
        return Result.success(userList);
    }

    /**
     * 检查当前用户是否为管理员
     */
    private void checkAdmin() {
        Integer role = UserContext.getCurrentRole();
        if (role == null || role != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
