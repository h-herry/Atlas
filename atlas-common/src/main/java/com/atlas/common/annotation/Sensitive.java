package com.atlas.common.annotation;

import com.atlas.common.serializer.SensitiveType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感数据注解 — 标记需要脱敏的字段，Jackson 序列化时自动替换为脱敏值 /
 * Sensitive data annotation — marks fields that need masking;
 * automatically replaced with masked value during Jackson serialization
 *
 * <p>支持类型 / Supported types:
 * <ul>
 *   <li>BANK_CARD — 银行账号，保留前4后4 / Bank account, keep first 4 + last 4</li>
 *   <li>PHONE — 手机号，保留前3后4 / Phone number, keep first 3 + last 4</li>
 *   <li>ID_CARD — 身份证号，保留前1后1 / ID card, keep first 1 + last 1</li>
 *   <li>EMAIL — 邮箱，保留首字符和域名 / Email, keep first char + domain</li>
 *   <li>ADDRESS — 详细地址，只保留省市 / Address, keep province + city only</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Sensitive {

    /** 脱敏类型，默认手机号 / Masking type, default phone */
    SensitiveType value() default SensitiveType.PHONE;
}
