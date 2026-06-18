package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Result 统一响应体测试")
class ResultTest {

    @Test
    @DisplayName("ok() 无参应返回 code=200 且 data 为 null")
    void should_return_success_with_null_data_when_ok_no_args() {
        Result<Void> result = Result.ok();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("ok(data) 应返回 code=200 且携带 data")
    void should_return_success_with_data_when_ok_with_data() {
        String data = "test-data";
        Result<String> result = Result.ok(data);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isEqualTo("test-data");
    }

    @Test
    @DisplayName("fail(ErrorCode) 应返回对应错误码和信息")
    void should_return_fail_with_error_code_when_fail_by_enum() {
        Result<Void> result = Result.fail(ErrorCode.ORDER_NOT_EXIST);

        assertThat(result.getCode()).isEqualTo(4001);
        assertThat(result.getMessage()).isEqualTo("采购订单不存在");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("fail(code, message) 应返回自定义错误码和信息")
    void should_return_fail_with_custom_code_and_message() {
        Result<Void> result = Result.fail(9999, "自定义错误");

        assertThat(result.getCode()).isEqualTo(9999);
        assertThat(result.getMessage()).isEqualTo("自定义错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("全参构造应正确赋值所有字段")
    void should_set_all_fields_via_all_args_constructor() {
        Result<String> result = new Result<>(200, "成功", "payload");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("成功");
        assertThat(result.getData()).isEqualTo("payload");
    }
}
