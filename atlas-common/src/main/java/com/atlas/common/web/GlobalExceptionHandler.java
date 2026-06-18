package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.core.exception.SystemException;
import com.atlas.common.core.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 — AOP 拦截所有异常，统一返回 Result /
 * Global exception handler — intercepts all exceptions via AOP, returns unified Result
 *
 * <p>分类处理 / Classification:
 * <ul>
 *   <li>BusinessException → HTTP 200 + code + message（业务规则约束，前端展示）</li>
 *   <li>SystemException → HTTP 500 + 通用错误（内部故障，不暴露堆栈）</li>
 *   <li>MethodArgumentNotValidException → HTTP 400 + 参数校验详情</li>
 *   <li>AccessDeniedException → HTTP 403</li>
 *   <li>Exception → HTTP 500（兜底，生产环境不暴露堆栈）</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        return Result.fail(ErrorCode.UNAUTHORIZED.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    /**
     * 业务异常 — HTTP 200 + 业务错误码（前端友好展示）/ Business exception
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBiz(BizException e) {
        log.warn("业务异常 [{}]: {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常 — HTTP 500，生产环境只返回通用错误，不暴露堆栈 /
     * System exception — HTTP 500, only generic error in production
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystem(SystemException e) {
        log.error("系统异常 [{}]: {}", e.getCode(), e.getMessage(), e);
        // 生产环境不暴露内部错误详情 / In production, do not expose internal error details
        return Result.fail(ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValid(Exception e) {
        String msg = "参数校验失败 / Parameter validation failed";
        if (e instanceof MethodArgumentNotValidException me) {
            msg = me.getBindingResult().getFieldErrors().stream()
                    .map(f -> f.getField() + " " + f.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b).orElse(msg);
        }
        log.warn("参数校验失败: {}", msg);
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败 / Parameter validation failed");
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 未知异常兜底 — HTTP 500，生产环境不暴露堆栈 /
     * Unknown exception fallback — HTTP 500, no stacktrace in production
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception e) {
        log.error("未知异常 / Unknown exception", e);
        // 生产环境只返回通用错误信息 / Production only returns generic error message
        return Result.fail(ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
