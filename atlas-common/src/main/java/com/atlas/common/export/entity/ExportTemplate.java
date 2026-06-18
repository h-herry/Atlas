package com.atlas.common.export.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据导出模板实体 — 预设模板含列定义，支持按模块导出 Excel /
 * Export template entity — preset templates with column definitions; supports per-module Excel export
 *
 * <p>columns 字段为 JSON 数组: [{"field":"supplierName","label":"供应商名称","width":20}] /
 * columns field is a JSON array defining export columns</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("export_template")
public class ExportTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long templateId;

    private String name;

    /** 所属模块: SUPPLIER / ORDER / SETTLEMENT / QUALITY / PERFORMANCE */
    private String module;

    /** 导出列定义 JSON / Export column definitions */
    private String columns;

    private Long creator;
    private String creatorName;

    /** 是否部门共享 / Is department-shared */
    private Integer isShared;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
