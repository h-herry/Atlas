package com.atlas.common.security.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DataScope 注解测试")
class DataScopeTest {

    @Test
    @DisplayName("默认值应正确")
    void should_have_correct_default_values() throws NoSuchMethodException {
        DataScope annotation = AnnotatedClass.class
                .getMethod("annotatedMethod")
                .getAnnotation(DataScope.class);

        assertThat(annotation.tableAlias()).isEqualTo("");
        assertThat(annotation.deptColumn()).isEqualTo("dept_id");
        assertThat(annotation.userColumn()).isEqualTo("created_by");
    }

    static class AnnotatedClass {
        @DataScope
        public void annotatedMethod() {}
    }
}
