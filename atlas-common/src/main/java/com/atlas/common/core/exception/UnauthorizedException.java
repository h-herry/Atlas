package com.atlas.common.core.exception;

import com.atlas.common.core.enums.ErrorCode;

/**
 * 未授权异常 — Security 认证入口抛出，区别于 403 权限不足 /
 * Unauthorized exception — thrown by Security auth entry, distinct from 403 Forbidden
 */
public class UnauthorizedException extends BizException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED.getCode(), message);
    }
}
