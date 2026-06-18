package com.atlas.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解 — 标注在 Controller 方法上，AOP 自动记录操作审计 / 
 * Audit log annotation — annotated on Controller methods, AOP auto-records operation audit
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 业务模块: USER / SUPPLIER / CONTRACT / PURCHASE / INVENTORY / RECEIPT / WORKFLOW / Business module */
    String module() default "";

    /** 操作类型: CREATE / UPDATE / DELETE / QUERY / LOGIN / APPROVE / SUBMIT / CONFIRM / Operation type */
    String operation() default "";

    /** 操作动作（简短中文说明）/ Action (brief Chinese) */
    String action() default "";

    /** 操作描述（简短中文说明） / Operation description (brief) */
    String description() default "";
}
