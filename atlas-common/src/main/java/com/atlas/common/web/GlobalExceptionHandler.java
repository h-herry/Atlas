package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
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

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBiz(BizException e) {
        log.warn("业务异常 [{}]: {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValid(Exception e) {
        String msg = "参数校验失败";
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
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception e) {
        log.error("未知异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
