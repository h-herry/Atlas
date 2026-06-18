package com.atlas.user.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败次数限制服务 — Redis 计数，5 次失败锁定 30 分钟 /
 * Login attempt limiting service — Redis counter, lock 30 min after 5 failures
 * <p>
 * Redis key: login:attempt:{userId}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ATTEMPT_KEY_PREFIX = "login:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    /**
     * 登录失败计数 +1，首次失败设置 30 分钟 TTL /
     * Increment failure count; set 30-min TTL on first failure
     */
    public void recordFailure(Long userId) {
        String key = ATTEMPT_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        log.warn("登录失败计数: userId={}, 当前次数={}", userId, count);
    }

    /**
     * 检查用户是否已被锁定 /
     * Check if user is locked due to excessive failures
     *
     * @return true 已锁定 / locked, false 未锁定 / not locked
     */
    public boolean isLocked(Long userId) {
        String key = ATTEMPT_KEY_PREFIX + userId;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        return count != null && count >= MAX_ATTEMPTS;
    }

    /**
     * 登录成功后清除失败计数 /
     * Clear failure count on successful login
     */
    public void clearAttempts(Long userId) {
        redisTemplate.delete(ATTEMPT_KEY_PREFIX + userId);
        log.debug("登录成功，清除失败计数: userId={}", userId);
    }

    /**
     * 获取剩余锁定秒数（0 表示未被锁定） /
     * Get remaining lock seconds (0 = not locked)
     */
    public long getRemainingLockSeconds(Long userId) {
        String key = ATTEMPT_KEY_PREFIX + userId;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
