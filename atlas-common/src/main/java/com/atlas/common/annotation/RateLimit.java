package com.atlas.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解 — 基于 Guava RateLimiter 的本地限流 /
 * API rate limit annotation — local rate limiting based on Guava RateLimiter
 *
 * <p>标注在 Controller 方法上，限制该接口的 QPS。
 * <br>触发限流时抛出 BizException(ErrorCode.RATE_LIMIT_EXCEEDED)。 /
 * Annotated on Controller methods to limit QPS. Throws BizException on rate limit trigger.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 每秒允许的请求数（QPS 阈值），默认 100 / Allowed requests per second (QPS threshold), default 100 */
    double value() default 100.0;

    /** 获取令牌的超时时间，默认 0（非阻塞，立即返回） / Token acquisition timeout, default 0 (non-blocking, immediate return) */
    long timeout() default 0;

    /** 超时时间单位，默认 MILLISECONDS / Timeout time unit, default MILLISECONDS */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
