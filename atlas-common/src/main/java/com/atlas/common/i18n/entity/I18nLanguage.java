package com.atlas.common.i18n.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 语言实体 — 支持的语言列表，可动态增删
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("i18n_language")
public class I18nLanguage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 语言代码：zh-CN / en-US / ja-JP */
    private String code;

    /** 语言名称：简体中文 / English / 日本語 */
    private String name;

    /** 本地名称：简体中文 / English / 日本語 */
    private String nativeName;

    /** 1启用 0禁用 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;

    private LocalDateTime createdAt;
}
