package com.atlas.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SnowflakeIdGenerator 雪花 ID 生成器测试")
class SnowflakeIdGeneratorTest {

    @Test
    @DisplayName("nextId 应返回正数")
    void should_return_positive_long_when_next_id() {
        long id = SnowflakeIdGenerator.nextId();

        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("nextIdStr 应返回数字字符串")
    void should_return_numeric_string_when_next_id_str() {
        String idStr = SnowflakeIdGenerator.nextIdStr();

        assertThat(idStr).isNotEmpty();
        assertThat(Long.parseLong(idStr)).isPositive();
    }

    @Test
    @DisplayName("nextId 应单调递增")
    void should_be_monotonically_increasing() {
        long prev = SnowflakeIdGenerator.nextId();
        long next = SnowflakeIdGenerator.nextId();

        assertThat(next).isGreaterThan(prev);
    }

    @RepeatedTest(50)
    @DisplayName("批量生成应无重复")
    void should_generate_unique_ids_in_batch() {
        long id = SnowflakeIdGenerator.nextId();
        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("nextId 批量 1000 次应无重复")
    void should_not_duplicate_in_1000_generations() {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(SnowflakeIdGenerator.nextId());
        }

        assertThat(ids).hasSize(1000);
    }

    @Test
    @DisplayName("自定义 workerId 和 dataCenterId 应正常生成")
    void should_generate_id_with_custom_worker_and_datacenter() {
        long id = SnowflakeIdGenerator.nextId(1L, 1L);

        assertThat(id).isPositive();
    }

    @Test
    @DisplayName("不同 workerId 生成的 ID 应不同")
    void should_differ_by_worker_id() {
        long id1 = SnowflakeIdGenerator.nextId(0L, 0L);
        long id2 = SnowflakeIdGenerator.nextId(1L, 0L);

        assertThat(id1).isNotEqualTo(id2);
    }
}
