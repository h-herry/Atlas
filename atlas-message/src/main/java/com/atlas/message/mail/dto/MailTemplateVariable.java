package com.atlas.message.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件模板变量替换项 — 单个变量的 key-value 映射 /
 * Mail template variable replacement item — single key-value variable mapping
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplateVariable {

    /** 模板占位符名称（不含 {} 包裹）/ Template placeholder name (without {} wrapping) */
    private String key;

    /** 变量值 / Variable value */
    private String value;

    /** 描述（用于文档）/ Description (for documentation) */
    private String description;
}
