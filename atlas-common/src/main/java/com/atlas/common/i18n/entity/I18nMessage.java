package com.atlas.common.i18n.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 翻译消息实体 — 存储所有语言的翻译文本
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("i18n_message")
public class I18nMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息键，如 common.success / supplier.not_exist */
    private String messageKey;

    /** 语言代码：zh-CN / en-US */
    private String languageCode;

    /** 翻译文本，支持 MessageFormat 占位符 {0} {1} */
    private String messageValue;

    /** 所属模块：common/user/supplier/contract/purchase/inventory/receipt/workflow/open */
    private String module;

    /** 描述 */
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
