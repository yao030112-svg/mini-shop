package com.minishop.util;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT Token 属性测试
 * 
 * **Validates: Requirements 1.3, 13.2**
 * 
 * 属性 12：Token有效性验证
 * - 对于任意格式错误或已过期的Token，系统应拒绝请求并返回401状态码
 * - 对于任意有效Token，系统应能正确解析出用户ID和角色信息
 */
class JwtUtilPropertyTest {

    /**
     * 属性：对于任意有效的 userId（正整数）和 role（0或1），
     * 生成的 Token 能正确解析出相同的 userId 和 role。
     * 
     * **Validates: Requirements 1.3, 13.2**
     */
    @Property
    void validTokenShouldParseCorrectUserIdAndRole(
            @ForAll @LongRange(min = 1, max = 999999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 1) Integer role) {

        String token = JwtUtil.generateToken(userId, role);
        Claims claims = JwtUtil.parseToken(token);

        assertThat(Long.parseLong(claims.getSubject())).isEqualTo(userId);
        assertThat(claims.get("role", Integer.class)).isEqualTo(role);
    }

    /**
     * 属性：对于任意格式错误的字符串（随机字符串），
     * parseToken 应抛出异常。
     * 
     * **Validates: Requirements 1.3, 13.2**
     */
    @Property
    void malformedTokenShouldThrowException(
            @ForAll @StringLength(min = 1, max = 200) @AlphaChars String randomString) {

        assertThatThrownBy(() -> JwtUtil.parseToken(randomString))
                .isInstanceOf(Exception.class);
    }

    /**
     * 属性：对于有效 Token，isTokenExpired 应返回 false。
     * 
     * **Validates: Requirements 13.2**
     */
    @Property
    void validTokenShouldNotBeExpired(
            @ForAll @LongRange(min = 1, max = 999999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 1) Integer role) {

        String token = JwtUtil.generateToken(userId, role);

        assertThat(JwtUtil.isTokenExpired(token)).isFalse();
    }

    /**
     * 属性：Token 有效期应为 7 天。
     * 验证生成的 Token 过期时间与当前时间之差约为 7 天（允许 5 秒误差）。
     * 
     * **Validates: Requirements 13.2**
     */
    @Property
    void tokenExpirationShouldBe7Days(
            @ForAll @LongRange(min = 1, max = 999999999L) Long userId,
            @ForAll @IntRange(min = 0, max = 1) Integer role) {

        long beforeGenerate = System.currentTimeMillis();
        String token = JwtUtil.generateToken(userId, role);
        long afterGenerate = System.currentTimeMillis();

        Claims claims = JwtUtil.parseToken(token);
        long expirationTime = claims.getExpiration().getTime();
        long issuedAtTime = claims.getIssuedAt().getTime();

        long expectedDuration = 7 * 24 * 60 * 60 * 1000L;
        long actualDuration = expirationTime - issuedAtTime;

        assertThat(actualDuration).isEqualTo(expectedDuration);

        // Also verify expiration is approximately 7 days from now
        long toleranceMs = 5000L; // 5 seconds tolerance
        assertThat(expirationTime).isBetween(
                beforeGenerate + expectedDuration - toleranceMs,
                afterGenerate + expectedDuration + toleranceMs);
    }

    /**
     * 属性：对于任意随机字节序列组成的字符串，isTokenExpired 应返回 true
     * （因为无效 token 被视为过期/无效）。
     * 
     * **Validates: Requirements 1.3**
     */
    @Property
    void invalidTokenShouldBeConsideredExpired(
            @ForAll @StringLength(min = 1, max = 100) @AlphaChars String randomString) {

        assertThat(JwtUtil.isTokenExpired(randomString)).isTrue();
    }
}
