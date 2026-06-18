package com.atlas.common.core.exception;

import com.atlas.common.core.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BizException 业务异常测试")
class BizExceptionTest {

    @Test
    @DisplayName("通过 ErrorCode 构造时应使用枚举 message")
    void should_use_enum_message_when_construct_with_error_code() {
        BizException ex = new BizException(ErrorCode.ORDER_NOT_EXIST);

        assertThat(ex.getCode()).isEqualTo(4001);
        assertThat(ex.getMessage()).isEqualTo("采购订单不存在");
    }

    @Test
    @DisplayName("通过 ErrorCode + 自定义 message 构造时应覆盖 message")
    void should_override_message_when_construct_with_error_code_and_custom_message() {
        BizException ex = new BizException(ErrorCode.ORDER_NOT_EXIST, "订单 12345 不存在");

        assertThat(ex.getCode()).isEqualTo(4001);
        assertThat(ex.getMessage()).isEqualTo("订单 12345 不存在");
    }

    @Test
    @DisplayName("通过 code + message 构造时应正确赋值")
    void should_set_code_and_message_when_construct_with_code_and_message() {
        BizException ex = new BizException(9999, "测试业务异常");

        assertThat(ex.getCode()).isEqualTo(9999);
        assertThat(ex.getMessage()).isEqualTo("测试业务异常");
    }

    @Test
    @DisplayName("BizException 应继承 RuntimeException")
    void should_extend_runtime_exception() {
        BizException ex = new BizException(ErrorCode.INTERNAL_ERROR);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("BizException 应可被 try-catch 捕获")
    void should_be_catchable_as_runtime_exception() {
        assertThatThrownBy(() -> {
            throw new BizException(ErrorCode.FORBIDDEN);
        }).isInstanceOf(BizException.class)
          .extracting("code")
          .isEqualTo(403);
    }
}
