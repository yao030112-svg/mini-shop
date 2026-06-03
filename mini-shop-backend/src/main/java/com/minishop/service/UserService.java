package com.minishop.service;

import com.minishop.entity.User;
import com.minishop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 禁用用户（将状态设为0）
     */
    public void disableUser(Long id) {
        userMapper.updateStatus(id, 0);
    }

    /**
     * 启用用户（将状态设为1）
     */
    public void enableUser(Long id) {
        userMapper.updateStatus(id, 1);
    }

    /**
     * 获取所有用户列表
     */
    public List<User> getUserList() {
        return userMapper.selectList();
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long userId) {
        return userMapper.findById(userId);
    }
}
