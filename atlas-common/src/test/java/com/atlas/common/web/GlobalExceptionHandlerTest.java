package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.core.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler 全局异常处理器测试")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("UnauthorizedException 应返回 401")
    void should_return_unauthorized_when_unauthorized_exception() {
        UnauthorizedException ex = new UnauthorizedException("Token 过期");

        Result<Void> result = handler.handleUnauthorized(ex);

        assertThat(result.getCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode());
        assertThat(result.getMessage()).isEqualTo("Token 过期");
    }

    @Test
    @DisplayName("AccessDeniedException 应返回 403")
    void should_return_forbidden_when_access_denied() {
        AccessDeniedException ex = new AccessDeniedException("权限不足");

        Result<Void> result = handler.handleAccessDenied(ex);

        assertThat(result.getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
        assertThat(result.getMessage()).isEqualTo(ErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("BizException 应返回对应业务错误码")
    void should_return_biz_error_when_biz_exception() {
        BizException ex = new BizException(ErrorCode.STOCK_INSUFFICIENT);

        Result<Void> result = handler.handleBiz(ex);

        assertThat(result.getCode()).isEqualTo(5001);
        assertThat(result.getMessage()).isEqualTo("库存不足");
    }

    @Test
    @DisplayName("BizException 自定义 message 应正确传递")
    void should_pass_custom_message_when_biz_exception_with_custom_message() {
        BizException ex = new BizException(ErrorCode.NOT_FOUND.getCode(), "商品 SKU-001 不存在");

        Result<Void> result = handler.handleBiz(ex);

        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("商品 SKU-001 不存在");
    }

    @Test
    @DisplayName("ConstraintViolationException 应返回参数校验失败")
    void should_return_bad_request_when_constraint_violation() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("名称不能为空");
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        Result<Void> result = handler.handleConstraint(ex);

        assertThat(result.getCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());
        assertThat(result.getMessage()).contains("名称不能为空");
    }

    @Test
    @DisplayName("未知 Exception 应返回 500")
    void should_return_internal_error_when_unknown_exception() {
        Exception ex = new RuntimeException("系统内部错误");

        Result<Void> result = handler.handleOther(ex);

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(result.getMessage()).isEqualTo(ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
