package com.minishop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minishop.entity.User;
import com.minishop.exception.ErrorCode;
import com.minishop.mapper.UserMapper;
import com.minishop.util.JwtUtil;
import com.minishop.vo.Result;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器
 * 从请求 Header 中提取 Token 并验证有效性
 * Token 验证成功后检查用户状态，禁用用户直接拒绝
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 检查 Authorization Header 是否存在
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 检查 Token 是否为空
        if (token.isEmpty()) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        try {
            // 解析 Token
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            Integer role = claims.get("role", Integer.class);

            // 查询用户并检查状态
            User user = userMapper.findById(userId);
            if (user == null) {
                writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
                return false;
            }

            // 检查用户是否被禁用（status=0 表示禁用）
            if (user.getStatus() != null && user.getStatus() == 0) {
                writeErrorResponse(response, ErrorCode.USER_DISABLED);
                return false;
            }

            // 将用户信息存入 ThreadLocal
            UserContext.setCurrentUser(userId, role);

            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(errorCode.getCode(), errorCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
