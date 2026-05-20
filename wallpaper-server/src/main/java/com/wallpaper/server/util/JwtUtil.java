package com.wallpaper.server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:wallpaper-server-jwt-secret-key-must-be-at-least-256-bits-long}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        log.info("JWT工具类初始化完成，过期时间：{}ms ({}小时)", expiration, expiration / 3600000);
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public boolean validateToken(String token) {
        try {
            log.debug("开始验证JWT Token, Token长度: {}", token != null ? token.length() : 0);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("JWT Token验证成功");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT Token签名无效: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT Token验证失败, 未知错误: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                return claims.getExpiration().before(new Date());
            }
        } catch (Exception e) {
            log.warn("检查Token过期时间失败: {}", e.getMessage());
        }
        return true;
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("解析JWT Token失败: {}", e.getMessage());
            return null;
        }
    }

    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Long userId = getUserIdFromToken(token);
                String username = getUsernameFromToken(token);
                String role = getRoleFromToken(token);
                return generateToken(userId, username, role);
            }
        } catch (Exception e) {
            log.warn("刷新Token失败: {}", e.getMessage());
        }
        return null;
    }
}
