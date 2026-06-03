package com.minishop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * JWT 工具类
 * 提供 Token 的生成、解析和过期判断功能
 */
public class JwtUtil {

    /** 签名密钥 */
    private static final String SECRET = "mini-shop-secret-key";

    /** Token 有效期：7天 */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @param role   用户角色（0普通用户 1管理员）
     * @return JWT Token 字符串
     */
    public static String generateToken(Long userId, Integer role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * 解析 JWT Token，返回 Claims
     *
     * @param token JWT Token 字符串
     * @return Claims 对象，包含 userId（subject）和 role
     * @throws ExpiredJwtException 当 Token 已过期时抛出
     * @throws io.jsonwebtoken.SignatureException 当签名验证失败时抛出
     * @throws io.jsonwebtoken.MalformedJwtException 当 Token 格式错误时抛出
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token 字符串
     * @return true 表示已过期，false 表示未过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
