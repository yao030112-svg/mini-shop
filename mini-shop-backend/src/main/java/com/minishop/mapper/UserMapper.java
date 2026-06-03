package com.minishop.mapper;

import com.minishop.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 根据微信openid查询用户
     */
    User findByOpenid(@Param("openid") String openid);

    /**
     * 新增用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 更新用户状态（禁用/启用）
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据昵称和角色查找用户（用于管理员登录）
     */
    User findByNicknameAndRole(@Param("nickname") String nickname, @Param("role") Integer role);

    /**
     * 查询所有用户列表（排除已删除）
     */
    List<User> selectList();

    /**
     * 统计所有用户数量
     */
    int countAll();

    /**
     * 统计指定时间范围内的新增用户数量
     */
    int countByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
