package com.atlas.common.cache.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁 — 基于 Redisson，封装 tryLock 模板方法 /
 * Redis distributed lock — based on Redisson, wraps tryLock template method
 * <p>
 * 面试要点 / Interview highlights：
 * 1. 看门狗机制：Redisson 自动续期，锁默认 30s，每 10s 续一次 / Watchdog: Redisson auto-renews lock (default 30s, renew every 10s)
 * 2. 可重入：Redis Hash key=线程ID, value=重入次数 / Reentrant: Redis Hash key=thread ID, value=reentry count
 * 3. 红锁：多 Redis 节点场景提升一致性（本项目单节点够用） / RedLock: improves consistency in multi-Redis scenarios (single node suffices here)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取锁并执行，自动释放 /
     * Try to acquire lock and execute, auto-release
     *
     * @param lockKey  锁唯一标识（建议 "业务:ID"，如 "order:12345"） / Lock unique key (recommend "business:ID", e.g. "order:12345")
     * @param waitTime 最大等待时长（秒） / Max wait time (seconds)
     * @param leaseTime 持有锁时长（秒，-1 启用看门狗自动续期） / Lock hold time (seconds, -1 enables watchdog auto-renewal)
     * @param supplier 获取锁成功后执行的回调 / Callback executed after lock acquisition
     */
    public <T> T execute(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("获取分布式锁失败: lockKey={}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** 简化版：waitTime=3s, leaseTime=-1(看门狗) / Simplified: waitTime=3s, leaseTime=-1 (watchdog) */
    public <T> T execute(String lockKey, Supplier<T> supplier) {
        return execute(lockKey, 3, -1, supplier);
    }
}
