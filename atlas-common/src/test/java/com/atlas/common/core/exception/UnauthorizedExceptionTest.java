package com.atlas.common.core.exception;

import com.atlas.common.core.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnauthorizedException 未授权异常测试")
class UnauthorizedExceptionTest {

    @Test
    @DisplayName("无参构造应使用 UNAUTHORIZED 错误码")
    void should_use_unauthorized_error_code_when_no_args() {
        UnauthorizedException ex = new UnauthorizedException();

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getMessage()).isEqualTo("未登录或Token已过期");
    }

    @Test
    @DisplayName("带 message 构造应使用 UNAUTHORIZED 错误码 + 自定义 message")
    void should_use_unauthorized_code_with_custom_message() {
        UnauthorizedException ex = new UnauthorizedException("Token 已失效");

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getMessage()).isEqualTo("Token 已失效");
    }

    @Test
    @DisplayName("应继承 BizException")
    void should_extend_biz_exception() {
        UnauthorizedException ex = new UnauthorizedException();

        assertThat(ex).isInstanceOf(BizException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
