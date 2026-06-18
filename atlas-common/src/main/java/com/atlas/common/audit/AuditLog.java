package com.atlas.common.audit;

import java.lang.annotation.*;

/**
 * 操作审计注解 — 标注在 Controller 方法上，自动记录操作日志 /
 * Operation audit annotation — annotate Controller methods to auto-record operation logs
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {

    /** 操作模块，如 "采购管理" / Operation module, e.g. "Purchase Management" */
    String module() default "";

    /** 操作类型，如 "创建订单" / Operation type, e.g. "Create Order" */
    String action() default "";

    /** 是否记录请求参数，默认 true / Whether to record request params, default true */
    boolean recordParams() default true;

    /** 是否记录返回结果，默认 false（避免过大数据） / Whether to record return value, default false (avoid large data) */
    boolean recordResult() default false;
}
