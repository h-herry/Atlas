package com.atlas.common.core.exception;

import lombok.Getter;

/**
 * 系统异常 — 表示非预期的内部错误（数据库故障、网络超时、第三方服务异常等）。
 * 由 GlobalExceptionHandler 统一拦截，生产环境只返回通用错误信息，不暴露堆栈。
 * <p>
 * System exception — represents unexpected internal errors (DB failure,
 * network timeout, third-party service exception, etc.).
 * Intercepted by GlobalExceptionHandler; in production, only generic error
 * message is returned without exposing stacktrace.
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
public class SystemException extends RuntimeException {

    /** 内部错误码（用于日志追踪，不直接返回给前端）/ Internal error code (for log tracing, not exposed to frontend) */
    private final int code;

    public SystemException(String message) {
        super(message);
        this.code = 500;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
