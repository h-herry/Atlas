package com.atlas.common.aspect;

import com.atlas.common.annotation.RateLimit;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 接口限流切面 — 基于 Guava RateLimiter 本地令牌桶 /
 * API rate limiting aspect — local token bucket based on Guava RateLimiter
 *
 * <p>与 {@link RateLimit @RateLimit} 注解配合使用。
 * <br>每个被注解的方法维护独立的 RateLimiter 实例，并发安全。
 * <br>未获取到令牌时抛出 BizException(ErrorCode.RATE_LIMIT_EXCEEDED)。 /
 * Works with @RateLimit annotation. Each annotated method holds an independent
 * RateLimiter instance, concurrency-safe. Throws BizException on rate limit trigger.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /** 缓存每个方法的 RateLimiter 实例，key 为 "类名#方法名" / Cache RateLimiter per method, key = "ClassName#methodName" */
    private final ConcurrentMap<String, RateLimiter> limiterCache = new ConcurrentHashMap<>();

    @Around("@annotation(com.atlas.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        double qps = rateLimit.value();

        // 获取或创建 RateLimiter 实例（并发安全） / Get or create RateLimiter instance (concurrency-safe)
        RateLimiter limiter = limiterCache.computeIfAbsent(key, k -> {
            log.info("创建限流器: key={} qps={}", k, qps);
            return RateLimiter.create(qps);
        });

        // 尝试获取令牌 / Try acquire token
        boolean acquired = limiter.tryAcquire(rateLimit.timeout(), rateLimit.timeUnit());
        if (!acquired) {
            log.warn("接口限流触发: key={} qps={}", key, qps);
            throw new BizException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return joinPoint.proceed();
    }
}
