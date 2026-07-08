package com.gym.security;

import com.gym.common.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成、解析、验证 Token
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long expire;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire}") Long expire) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expire = expire;
    }

    /**
     * 生成 Token（会员）
     */
    public String generateMemberToken(Long userId, String role) {
        return generateToken(userId, Constants.USER_TYPE_MEMBER, role);
    }

    /**
     * 生成 Token（员工）
     */
    public String generateEmployeeToken(Long userId, String role) {
        return generateToken(userId, Constants.USER_TYPE_EMPLOYEE, role);
    }

    private String generateToken(Long userId, String userType, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(Constants.CLAIM_USER_ID, userId)
                .claim(Constants.CLAIM_USER_TYPE, userType)
                .claim(Constants.CLAIM_ROLE, role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中解析 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(Constants.CLAIM_USER_ID, Long.class);
    }

    /**
     * 从 Token 中获取用户类型
     */
    public String getUserType(String token) {
        return parseToken(token).get(Constants.CLAIM_USER_TYPE, String.class);
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRole(String token) {
        return parseToken(token).get(Constants.CLAIM_ROLE, String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取 Token 剩余过期时间 (ms)
     */
    public long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
