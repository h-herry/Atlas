package com.atlas.common.security.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 — 标注在 Mapper 方法上，MyBatis Plugin 自动追加数据范围过滤 SQL /
 * Data scope annotation — annotated on Mapper methods, MyBatis Plugin auto-appends data scope filter SQL
 * <p>
 * 使用场景：部门经理只能查本部门及子部门的订单 /
 * Use case: department manager can only query orders within own department and sub-departments
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataScope {

    /** 表别名，如 "o" (对应 order 表) / Table alias, e.g. "o" (for order table) */
    String tableAlias() default "";

    /** 部门字段名，如 "dept_id" / Department column name, e.g. "dept_id" */
    String deptColumn() default "dept_id";

    /** 用户字段名（本人数据），如 "created_by" / User column name (self data), e.g. "created_by" */
    String userColumn() default "created_by";
}
