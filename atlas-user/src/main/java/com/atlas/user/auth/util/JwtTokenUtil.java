package com.atlas.user.auth.util;

import com.atlas.user.auth.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token 工具类 — 双通道（enterprise/supplier）生成/解析/校验 /
 * JWT Token utility — dual-channel (enterprise/supplier) generate/parse/validate
 * <p>
 * 不同通道使用不同 issuer，便于网关层区分请求来源。
 * Different channels use different issuers for gateway-level source identification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    // ==================== Token 生成 / Token Generation ====================

    /**
     * 生成 JWT — 企业端通道 /
     * Generate JWT — enterprise channel
     */
    public String createEnterpriseToken(Long userId, String username, Map<String, Object> extraClaims) {
        return buildToken(userId, username, "enterprise", jwtProperties.getExpiration(), extraClaims);
    }

    /**
     * 生成 JWT — 供应商端通道 /
     * Generate JWT — supplier channel
     */
    public String createSupplierToken(Long userId, String username, Map<String, Object> extraClaims) {
        return buildToken(userId, username, "supplier", jwtProperties.getExpiration(), extraClaims);
    }

    /**
     * 生成 JWT（自定义过期秒数，供刷新 Token 使用） /
     * Generate JWT with custom expiration seconds (for token refresh)
     */
    public String createToken(Long userId, String username, String channel, long expireSeconds,
                              Map<String, Object> extraClaims) {
        return buildToken(userId, username, channel, expireSeconds, extraClaims);
    }

    private String buildToken(Long userId, String username, String channel, long expireSeconds,
                              Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000);

        String issuer = "enterprise".equals(channel)
                ? jwtProperties.getEnterpriseIssuer()
                : jwtProperties.getSupplierIssuer();

        JwtBuilder builder = Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .claim("username", username)
                .claim("channel", channel)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey());

        if (extraClaims != null && !extraClaims.isEmpty()) {
            extraClaims.forEach(builder::claim);
        }
        return builder.compact();
    }

    // ==================== Token 解析 / Token Parsing ====================

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
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 Token 是否有效（未过期 + 签名正确） /
     * Check whether token is valid (not expired + correct signature)
     */
    public boolean isValid(String token) {
        return parseToken(token) != null;
    }

    // ==================== Claims 快捷读取 / Claims Accessors ====================

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getChannel(Claims claims) {
        return claims.get("channel", String.class);
    }

    /**
     * 获取 Token 剩余有效期（秒） / Get remaining validity in seconds
     */
    public long getRemainingSeconds(Claims claims) {
        long remain = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remain, 0) / 1000;
    }

    /**
     * 计算 Token 的 SHA-256 哈希（用于黑名单 key） /
     * Compute SHA-256 hash of token (for blacklist key)
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
