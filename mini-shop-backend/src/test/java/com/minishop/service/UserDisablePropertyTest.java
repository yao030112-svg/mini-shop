package com.minishop.service;

import com.minishop.entity.User;
import com.minishop.mapper.UserMapper;
import com.minishop.security.JwtInterceptor;
import com.minishop.util.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * 用户禁用后请求拒绝 属性测试
 *
 * **Validates: Requirements 12.2**
 *
 * 属性 11：用户禁用后请求拒绝
 * - 对于任意被禁用的用户，使用该用户Token发起的任何需要认证的API请求应返回禁用提示并拒绝执行
 */
class UserDisablePropertyTest {

    /**
     * 属性：对于任意用户ID，当用户被禁用后（status=0），
     * 使用该用户的有效 Token 发起请求时，JwtInterceptor 应拒绝请求（preHandle 返回 false）。
     *
     * **Validates: Requirements 12.2**
     */
    @Property
    void disabledUserRequestShouldBeRejected(
            @ForAll @LongRange(min = 1, max = 999999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 1) Integer role) throws Exception {

        // 生成有效 Token
        String token = JwtUtil.generateToken(userId, role);

        // Mock UserMapper 返回禁用用户（status=0）
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        User disabledUser = new User();
        disabledUser.setId(userId);
        disabledUser.setStatus(0); // 禁用状态
        disabledUser.setRole(role);
        Mockito.when(userMapper.findById(userId)).thenReturn(disabledUser);

        // 创建 JwtInterceptor 并注入 mock 的 UserMapper
        JwtInterceptor interceptor = new JwtInterceptor();
        Field userMapperField = JwtInterceptor.class.getDeclaredField("userMapper");
        userMapperField.setAccessible(true);
        userMapperField.set(interceptor, userMapper);

        // 构造带有有效 Token 的请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.setMethod("GET");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // 验证拦截器拒绝请求
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();

        // 验证响应中包含禁用错误码 1005
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("1005");
        assertThat(responseBody).contains("用户已被禁用");
    }

    /**
     * 属性：对于任意用户ID，当用户状态正常（status=1）时，
     * 使用该用户的有效 Token 发起请求时，JwtInterceptor 应允许请求通过（preHandle 返回 true）。
     *
     * 这是对比属性，确保只有禁用用户被拒绝，正常用户不受影响。
     *
     * **Validates: Requirements 12.2**
     */
    @Property
    void activeUserRequestShouldBeAllowed(
            @ForAll @LongRange(min = 1, max = 999999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 1) Integer role) throws Exception {

        // 生成有效 Token
        String token = JwtUtil.generateToken(userId, role);

        // Mock UserMapper 返回正常用户（status=1）
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        User activeUser = new User();
        activeUser.setId(userId);
        activeUser.setStatus(1); // 正常状态
        activeUser.setRole(role);
        Mockito.when(userMapper.findById(userId)).thenReturn(activeUser);

        // 创建 JwtInterceptor 并注入 mock 的 UserMapper
        JwtInterceptor interceptor = new JwtInterceptor();
        Field userMapperField = JwtInterceptor.class.getDeclaredField("userMapper");
        userMapperField.setAccessible(true);
        userMapperField.set(interceptor, userMapper);

        // 构造带有有效 Token 的请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.setMethod("GET");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // 验证拦截器允许请求通过
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
}
