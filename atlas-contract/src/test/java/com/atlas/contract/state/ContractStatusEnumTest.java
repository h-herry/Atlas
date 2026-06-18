package com.atlas.contract.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("合同状态机单元测试")
class ContractStatusEnumTest {

    // ======================== 合法状态跳转 ========================

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        0 | 1
        1 | 2
        1 | 4
        2 | 3
        2 | 4
        3 | 5
        4 | 1
        5 | 6
        6 | 7
        6 | 8
        6 | 9
        7 | 6
    """)
    @DisplayName("合法状态跳转：from → to 通过校验")
    void testValidTransitions(int from, int to) {
        assertThatCode(() -> ContractStatusEnum.validateTransition(from, to))
                .doesNotThrowAnyException();
    }

    // ======================== 非法状态跳转 ========================

    @Test
    @DisplayName("非法跳转：已签署不能回退到草稿，抛异常")
    void testInvalidTransition() {
        assertThatThrownBy(() -> ContractStatusEnum.validateTransition(5, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已签署")
                .hasMessageContaining("草稿");
    }

    @Test
    @DisplayName("非法跳转：终态已终止不能再变更为执行中")
    void testTerminatedCannotTransition() {
        assertThatThrownBy(() -> ContractStatusEnum.validateTransition(8, 6))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已终止");
    }

    @Test
    @DisplayName("非法跳转：终态已完成不能再变更为变更中")
    void testCompletedCannotTransition() {
        assertThatThrownBy(() -> ContractStatusEnum.validateTransition(9, 7))
                .isInstanceOf(IllegalStateException.class);
    }

    // ======================== 所有状态都有跳转目标 ========================

    @Test
    @DisplayName("所有状态都有合法跳转目标（终态允许空集合）")
    void testAllStatusesHaveTargets() {
        for (ContractStatusEnum status : ContractStatusEnum.values()) {
            assertThat(status.getAllowTargets())
                    .as("状态 %s(%d) 的 allowTargets 不能为 null", status.getDesc(), status.getCode())
                    .isNotNull();

            // 终态（TERMINATED / COMPLETED）允许空集合，其他状态至少有一个目标
            if (status == ContractStatusEnum.TERMINATED || status == ContractStatusEnum.COMPLETED) {
                assertThat(status.getAllowTargets())
                        .as("终态 %s 应为空集合", status.getDesc())
                        .isEmpty();
            } else {
                assertThat(status.getAllowTargets())
                        .as("非终态 %s 至少应有一个跳转目标", status.getDesc())
                        .isNotEmpty();
            }
        }
    }

    // ======================== 枚举值映射 ========================

    @Test
    @DisplayName("根据 code 正确获取枚举值")
    void testOf() {
        assertThat(ContractStatusEnum.of(0)).isEqualTo(ContractStatusEnum.DRAFT);
        assertThat(ContractStatusEnum.of(5)).isEqualTo(ContractStatusEnum.SIGNED);
        assertThat(ContractStatusEnum.of(9)).isEqualTo(ContractStatusEnum.COMPLETED);
    }

    @Test
    @DisplayName("无效 code 抛 IllegalArgumentException")
    void testOf_Invalid() {
        assertThatThrownBy(() -> ContractStatusEnum.of(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("canTransitionTo 正确判断跳转合法性")
    void testCanTransitionTo() {
        assertThat(ContractStatusEnum.DRAFT.canTransitionTo(1)).isTrue();
        assertThat(ContractStatusEnum.DRAFT.canTransitionTo(5)).isFalse();
        assertThat(ContractStatusEnum.COMPLETED.canTransitionTo(0)).isFalse();
    }
}
