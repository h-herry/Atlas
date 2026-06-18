package com.atlas.common.security.annotation;

import java.lang.annotation.*;

/**
 * 功能权限注解 — 标注在 Controller 方法上，配合 Security 切面校验 /
 * Functional permission annotation — annotated on Controller methods, validated by Security aspect
 * <p>
 * 示例 / Example: @RequirePermission("purchase:order:create")
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirePermission {

    String value();

    /** 校验逻辑：AND / OR，默认 AND 需要同时满足所有权限 / Validation logic: AND / OR, default AND requires all permissions */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
