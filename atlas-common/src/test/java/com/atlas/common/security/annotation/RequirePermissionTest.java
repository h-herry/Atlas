package com.atlas.common.security.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RequirePermission 注解测试")
class RequirePermissionTest {

    @Test
    @DisplayName("Logical 枚举应包含 AND 和 OR")
    void should_have_and_or_in_logical_enum() {
        assertThat(RequirePermission.Logical.values()).containsExactly(
            RequirePermission.Logical.AND,
            RequirePermission.Logical.OR
        );
    }

    @Test
    @DisplayName("Logical.AND 默认值为 AND")
    void should_default_to_and() {
        assertThat(RequirePermission.Logical.AND.name()).isEqualTo("AND");
    }
}
