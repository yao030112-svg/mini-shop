package com.minishop.controller;

import com.minishop.dto.AdminLoginRequest;
import com.minishop.dto.WxLoginRequest;
import com.minishop.service.AuthService;
import com.minishop.vo.LoginVO;
import com.minishop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信登录接口
     *
     * @param request 包含微信登录 code 的请求体
     * @return 包含 token 和用户信息的响应
     */
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        LoginVO loginVO = authService.wxLogin(request.getCode());
        return Result.success(loginVO);
    }

    /**
     * 管理员登录接口
     *
     * @param request 包含管理员用户名和密码的请求体
     * @return 包含带管理员角色标识的 token 和用户信息的响应
     */
    @PostMapping("/admin-login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        LoginVO loginVO = authService.adminLogin(request.getUsername(), request.getPassword());
        return Result.success(loginVO);
    }
}
