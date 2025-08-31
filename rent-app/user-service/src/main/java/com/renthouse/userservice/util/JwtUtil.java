package com.renthouse.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    private static final String SECRET_KEY = "rent123rent123rent123rent123rent123"; // 至少32字节
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    public String generateToken(String username, String role, Integer userId) {
        System.out.println("生成JWT令牌 - 用户名: " + username + ", 角色: " + role + ", 用户ID: " + userId);
        
        if (role == null) {
            System.out.println("警告：角色为null，设置默认角色USER");
            role = "USER";
        }
        
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
        
        System.out.println("生成的令牌: " + token);
        
        // 验证生成的令牌是否包含role
        try {
            Claims claims = extractClaims(token);
            System.out.println("验证令牌内容 - 用户名: " + claims.getSubject() + ", 角色: " + claims.get("role") + ", 用户ID: " + claims.get("userId"));
        } catch (Exception e) {
            System.out.println("令牌验证失败: " + e.getMessage());
        }
        
        return token;
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String extractUsername(String token) {
        String username = extractClaims(token).getSubject();
        System.out.println("从JWT提取用户名: " + username);
        return username;
    }
    
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        String role = claims.get("role", String.class);
        System.out.println("从JWT提取角色: " + role);
        return role;
    }
    
    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }
    
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            System.out.println("令牌验证 - 用户名: " + claims.getSubject() + ", 角色: " + claims.get("role") + ", 过期时间: " + claims.getExpiration());
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.out.println("令牌验证失败: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}