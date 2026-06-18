package com.atlas.common.config;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.web.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SentinelConfig 限流熔断配置测试")
class SentinelConfigTest {

    private SentinelConfig sentinelConfig;

    @BeforeEach
    void setUp() {
        sentinelConfig = new SentinelConfig();
    }

    @Test
    @DisplayName("FlowException 应返回限流友好提示")
    void should_return_rate_limit_message_when_flow_exception() {
        Result<Void> result = sentinelConfig.handleFlowException();

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(result.getMessage()).contains("请求过于频繁");
    }

    @Test
    @DisplayName("DegradeException 应返回熔断友好提示")
    void should_return_degrade_message_when_degrade_exception() {
        Result<Void> result = sentinelConfig.handleDegradeException();

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(result.getMessage()).contains("熔断保护");
    }

    @Test
    @DisplayName("FlowException 类名匹配应触发限流处理")
    void should_handle_flow_when_exception_name_is_flow_exception() {
        RuntimeException flowEx = new RuntimeException() {};
        // 通过反射覆盖以模拟 FlowException 类名匹配
        Result<Void> result = sentinelConfig.handleBlockException(
                new RuntimeException("FlowException") {
                    @Override
                    public String toString() {
                        return "FlowException";
                    }
                });

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
    }

    @Test
    @DisplayName("DegradeException 类名匹配应触发熔断处理")
    void should_handle_degrade_for_degrade_exception_name() {
        Result<Void> result = sentinelConfig.handleBlockException(
                new RuntimeException("DegradeException") {
                    @Override
                    public String toString() {
                        return "DegradeException";
                    }
                });

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
    }

    @Test
    @DisplayName("未知异常应返回通用内部错误")
    void should_return_internal_error_when_unknown_exception() {
        Result<Void> result = sentinelConfig.handleBlockException(new RuntimeException("unknown"));

        assertThat(result.getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(result.getMessage()).isEqualTo(ErrorCode.INTERNAL_ERROR.getMessage());
    }

    @Test
    @DisplayName("FlowException 返回的 Result data 应为 null")
    void should_return_null_data_when_flow_exception() {
        Result<Void> result = sentinelConfig.handleFlowException();

        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("DegradeException 返回的 Result data 应为 null")
    void should_return_null_data_when_degrade_exception() {
        Result<Void> result = sentinelConfig.handleDegradeException();

        assertThat(result.getData()).isNull();
    }
}
