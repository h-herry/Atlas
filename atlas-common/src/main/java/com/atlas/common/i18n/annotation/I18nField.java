package com.atlas.common.i18n.annotation;

import java.lang.annotation.*;

/**
 * 标记 VO/实体中的字段可以被 i18n 翻译
 *
 * <pre>
 * public class OrderVO {
 *     &#64;I18nField
 *     private String statusText; // 翻译前是 "PENDING"，翻译后是 "待审批"
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface I18nField {
}
