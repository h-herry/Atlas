package com.atlas.user.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务 — Redis 存储，key 为 Token SHA-256 哈希 /
 * Token blacklist service — Redis-stored, keyed by Token SHA-256 hash
 * <p>
 * 黑名单 key: token:blacklist:{tokenHash}
 * TTL = Token 剩余有效期，到期自动清理。
 * TTL = remaining token validity, auto-clean on expiry.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    /**
     * 将 Token 加入黑名单 /
     * Add token to blacklist
     *
     * @param tokenHash Token SHA-256 哈希 / Token SHA-256 hash
     * @param ttlSeconds 黑名单有效期（秒），应与 Token 剩余有效期一致 / TTL in seconds, should match remaining token validity
     */
    public void blacklist(String tokenHash, long ttlSeconds) {
        redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + tokenHash, "1", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Token 已加入黑名单: {}..., TTL={}s", tokenHash.substring(0, 12), ttlSeconds);
    }

    /**
     * 检查 Token 是否在黑名单中 /
     * Check if token is blacklisted
     *
     * @param tokenHash Token SHA-256 哈希 / Token SHA-256 hash
     * @return true 已拉黑 / blacklisted, false 未拉黑 / not blacklisted
     */
    public boolean isBlacklisted(String tokenHash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + tokenHash));
    }
}
