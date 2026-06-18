package com.atlas.common.serializer;

import com.atlas.common.annotation.Sensitive;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 敏感数据 Jackson 序列化器 — 根据 @Sensitive 注解自动替换为脱敏值 /
 * Sensitive data Jackson serializer — auto-replaces field value with masked version
 * based on @Sensitive annotation
 *
 * <p>使用方式 / Usage:
 * <pre>
 * public class SupplierDTO {
 *     {@code @Sensitive(SensitiveType.BANK_CARD)}
 *     private String bankAccount;  // 6222****1234
 *
 *     {@code @Sensitive(SensitiveType.PHONE)}
 *     private String phone;         // 138****5678
 * }
 * </pre>
 *
 * <p>日志脱敏 / Log masking: 在 {@code toString()} 或 log 输出中调用
 * {@link #mask(String, SensitiveType)} 静态方法完成脱敏。</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
public class SensitiveDataSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private static final char MASK_CHAR = '*';

    private SensitiveType type;

    /** 无参构造（Jackson 反射用）/ No-arg constructor (for Jackson reflection) */
    public SensitiveDataSerializer() {
    }

    public SensitiveDataSerializer(SensitiveType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }
        gen.writeString(mask(value, type));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Sensitive ann = property.getAnnotation(Sensitive.class);
            if (ann != null) {
                return new SensitiveDataSerializer(ann.value());
            }
        }
        return this;
    }

    /**
     * 静态脱敏方法 — 供日志输出等非 Jackson 场景使用 /
     * Static mask method — for non-Jackson scenarios like log output
     *
     * @param value 原始值 / original value
     * @param type  脱敏类型 / masking type
     * @return 脱敏后字符串 / masked string
     */
    public static String mask(String value, SensitiveType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int len = value.length();
        switch (type) {
            case BANK_CARD:
                if (len <= 8) return maskAll(value);
                return value.substring(0, 4) + repeat(MASK_CHAR, len - 8) + value.substring(len - 4);

            case PHONE:
                if (len <= 7) return maskAll(value);
                return value.substring(0, 3) + repeat(MASK_CHAR, len - 7) + value.substring(len - 4);

            case ID_CARD:
                if (len <= 2) return maskAll(value);
                return value.charAt(0) + repeat(MASK_CHAR, len - 2) + value.charAt(len - 1);

            case EMAIL:
                int atIdx = value.indexOf('@');
                if (atIdx <= 1) return maskAll(value);
                return value.charAt(0) + repeat(MASK_CHAR, atIdx - 1) + value.substring(atIdx);

            case ADDRESS:
                // 保留前6个字符（约省市信息），其余脱敏 / Keep first 6 chars (approx province+city), mask rest
                if (len <= 6) return value;
                return value.substring(0, 6) + repeat(MASK_CHAR, len - 6);

            default:
                return maskAll(value);
        }
    }

    private static String maskAll(String value) {
        return repeat(MASK_CHAR, value.length());
    }

    private static String repeat(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
