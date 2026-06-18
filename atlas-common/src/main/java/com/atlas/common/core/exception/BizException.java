package com.atlas.common.core.exception;

import com.atlas.common.core.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常 — ControllerAdvice 会统一拦截并转为 Result /
 * Business exception — intercepted by ControllerAdvice and converted to Result
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
