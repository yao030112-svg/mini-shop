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

    public LoginVO wxLogin(String code, String nickname, String avatarUrl) {
        String openid = wxApiService.code2Session(code);

        User user = userMapper.findByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname != null && !nickname.isBlank() ? nickname : "微信用户");
            user.setAvatarUrl(avatarUrl);
            user.setStatus(1);
            user.setRole(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setDeleted(0);
            userMapper.insert(user);
        } else {
            boolean needUpdate = false;
            if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                needUpdate = true;
            }
            if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                needUpdate = true;
            }
            if (needUpdate) {
                userMapper.update(user);
            }
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String token = JwtUtil.generateToken(user.getId(), user.getRole());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatarUrl(user.getAvatarUrl());
        loginVO.setRole(user.getRole());
        return loginVO;
    }

    public LoginVO adminLogin(String username, String password) {
        User admin = userMapper.findByNicknameAndRole(username, 1);
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "管理员账号不存在");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (admin.getPassword() == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "密码错误");
        }

        String token = JwtUtil.generateToken(admin.getId(), admin.getRole());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(admin.getId());
        loginVO.setNickname(admin.getNickname());
        loginVO.setAvatarUrl(admin.getAvatarUrl());
        loginVO.setRole(admin.getRole());
        return loginVO;
    }
}
