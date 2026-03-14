package com.seckill.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌工具类（适配 JJWT 0.11.5）
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:seckillSecretKey2026}")
    private String secret;
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    /**
     * 生成签名密钥 - 使用安全的密钥生成器
     */
    private SecretKey getSigningKey() {
        // 直接生成一个安全的密钥，不使用配置的字符串
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    /**
     * 生成令牌
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())  // 直接使用生成的密钥
                .compact();
    }

    /**
     * 解析令牌
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())  // 解析时使用相同的密钥生成逻辑
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("令牌已过期: {}", e.getMessage());
            throw new RuntimeException("登录已过期，请重新登录");
        } catch (JwtException e) {
            log.error("令牌无效: {}", e.getMessage());
            throw new RuntimeException("无效的令牌");
        }
    }
    
    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
}