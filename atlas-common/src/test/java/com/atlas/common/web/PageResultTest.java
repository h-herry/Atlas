package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResult 分页响应体测试")
class PageResultTest {

    @Test
    @DisplayName("构造方法应正确设置分页字段")
    void should_set_pagination_fields_correctly() {
        List<String> records = Arrays.asList("a", "b", "c");
        PageResult<String> pr = new PageResult<>(100L, 1L, 10L, records);

        assertThat(pr.getTotal()).isEqualTo(100L);
        assertThat(pr.getPage()).isEqualTo(1L);
        assertThat(pr.getSize()).isEqualTo(10L);
        assertThat(pr.getRecords()).hasSize(3);
    }

    @Test
    @DisplayName("ok 静态工厂应包装为 Result<PageResult>")
    void should_wrap_into_result_when_ok_static_factory() {
        List<Integer> records = Arrays.asList(1, 2);
        Result<PageResult<Integer>> result = PageResult.ok(25L, 2L, 10L, records);

        assertThat(result.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
        assertThat(result.getData().getTotal()).isEqualTo(25L);
        assertThat(result.getData().getRecords()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("空记录分页应正常返回")
    void should_handle_empty_records() {
        Result<PageResult<Object>> result = PageResult.ok(0L, 1L, 10L, Collections.emptyList());

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(0L);
        assertThat(result.getData().getRecords()).isEmpty();
    }
}
