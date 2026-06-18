package com.atlas.common.core.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类 — 生成 / 解析 / 校验 Token /
 * JWT utility class — generate / parse / validate tokens
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:atlas-jwt-default-secret-key-2024-production-change-in-config}")
    private String base64Secret;

    @Value("${jwt.expiration:7200}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    /**
     * 生成 JWT，claims 中存放 userId、username、deptId 等 /
     * Generate JWT, storing userId, username, deptId etc. in claims
     */
    public String createToken(Long userId, String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiration * 1000);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey());

        if (extraClaims != null && !extraClaims.isEmpty()) {
            extraClaims.forEach(builder::claim);
        }
        return builder.compact();
    }

    /**
     * 基于 claims 和过期秒数创建 JWT（供应商门户专用 / Supplier portal only） /
     * Create JWT based on claims map and expiration seconds (for supplier portal)
     *
     * @param claims         claims map (subject, username, deptId etc.)
     * @param expireSeconds  expiration in seconds
     * @return JWT token string
     */
    public String createToken(Map<String, Object> claims, long expireSeconds) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000);

        JwtBuilder builder = Jwts.builder()
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey());

        claims.forEach(builder::claim);
        return builder.compact();
    }

    /**
     * 解析 Token，验证失败返回 null /
     * Parse token, returns null on validation failure
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }
}
