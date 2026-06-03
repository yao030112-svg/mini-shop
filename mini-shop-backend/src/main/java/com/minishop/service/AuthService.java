package com.minishop.service;

import com.minishop.entity.User;
import com.minishop.exception.BusinessException;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.UserMapper;
import com.minishop.util.JwtUtil;
import com.minishop.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WxApiService wxApiService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 微信登录
     * 使用真实的微信code2Session接口
     *
     * @param code 微信登录 code
     * @return 登录响应（包含 token 和用户信息）
     */
    public LoginVO wxLogin(String code) {
        // 调用微信API获取openid
        String openid = wxApiService.code2Session(code);

        // 根据 openid 查找用户
        User user = userMapper.findByOpenid(openid);

        // 首次登录自动创建用户
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setAvatarUrl("");
            user.setStatus(1);
            user.setRole(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setDeleted(0);
            userMapper.insert(user);
        }

        // 检查用户是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 生成 JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getRole());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatarUrl(user.getAvatarUrl());

        return loginVO;
    }

    /**
     * 管理员登录
     *
     * @param username 管理员用户名（对应 nickname 字段）
     * @param password 管理员密码
     * @return 登录响应（包含带管理员角色的 token 和用户信息）
     */
    public LoginVO adminLogin(String username, String password) {
        // 根据用户名（nickname）和角色（1=管理员）查找管理员用户
        User admin = userMapper.findByNicknameAndRole(username, 1);

        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "管理员账号不存在");
        }

        // 检查用户是否被禁用
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 验证密码（BCrypt）
        if (admin.getPassword() == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "密码错误");
        }

        // 生成带管理员角色标识的 JWT Token
        String token = JwtUtil.generateToken(admin.getId(), admin.getRole());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(admin.getId());
        loginVO.setNickname(admin.getNickname());
        loginVO.setAvatarUrl(admin.getAvatarUrl());

        return loginVO;
    }
}
