package com.atlas.common.security.service;

import com.atlas.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务 — Redis 存储用户角色权限，实现无状态 Token 续期 /
 * Token service — stores user role/permission in Redis, implements stateless token renewal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String PERM_KEY = "user:perm:";
    private static final String TOKEN_BLACKLIST = "token:blacklist:";

    public List<String> getPermissions(Long userId) {
        return (List<String>) redisTemplate.opsForValue()
                .get(PERM_KEY + userId);
    }

    public void cachePermissions(Long userId, List<String> permissions) {
        redisTemplate.opsForValue().set(PERM_KEY + userId, permissions, 2, TimeUnit.HOURS);
    }

    public void renewIfNeeded(String token, Claims claims) {
        // Token 剩余有效期 < 配置的 50% 时签发新 Token（通过响应头下发） / Issue new token if remaining < 50% (via response header)
        long remain = claims.getExpiration().getTime() - System.currentTimeMillis();
        long half = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 2;
        if (remain < half) {
            log.debug("Token即将过半，发起续期: remain={}ms", remain);
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String newToken = jwtUtil.createToken(userId, username, null);
            RenewTokenHolder.set(newToken);
            log.info("Token续期已生成: userId={}", userId);
        }
    }

    public void blacklist(String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(TOKEN_BLACKLIST + token, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST + token));
    }
}
