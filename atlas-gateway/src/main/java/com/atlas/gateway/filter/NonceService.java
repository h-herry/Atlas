package com.atlas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Nonce 防重服务 — 基于 Redis SETNX 保证 nonce 五分钟内不重复 /
 * Nonce anti-replay service — uses Redis SETNX to ensure nonce uniqueness within 5 minutes
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
public class NonceService {

    private static final String NONCE_PREFIX = "atlas:nonce:";
    private static final Duration NONCE_TTL = Duration.ofMinutes(5);

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public NonceService(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    /**
     * 尝试获取 nonce 锁。成功返回 true，表示 nonce 未使用过；
     * 失败返回 false，表示该 nonce 已被使用（重放攻击）。
     * <p>
     * Try to acquire nonce lock. Returns true if nonce is fresh;
     * returns false if nonce has been used (replay attack).
     *
     * @param nonce 客户端随机数 / client random nonce
     * @return true=通过, false=重放 / true=passed, false=replay
     */
    public boolean tryAcquire(String nonce) {
        String key = NONCE_PREFIX + nonce;
        Boolean success = reactiveStringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", NONCE_TTL)
                .block();
        if (Boolean.TRUE.equals(success)) {
            log.debug("[NONCE] nonce={} 校验通过", nonce);
            return true;
        }
        log.warn("[NONCE] nonce={} 重复使用，疑似重放攻击", nonce);
        return false;
    }
}
