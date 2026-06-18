package com.atlas.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同条款库实体 — 条款独立存储与版本化管理 /
 * Contract clause library entity — independently stored and versioned clauses
 *
 * <p>分类: LAW 法律 / COMMERCIAL 商务 / STANDARD 通用，支持按分类查询和版本追溯 /
 * Categories: LAW / COMMERCIAL / STANDARD; supports category queries and version tracing</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("contract_clause")
public class ContractClause {

    @TableId(type = IdType.ASSIGN_ID)
    private Long clauseId;

    /** 条款编码 / Clause code */
    private String clauseCode;

    /** 条款分类: LAW / COMMERCIAL / STANDARD */
    private String category;

    /** 条款标题 / Clause title */
    private String title;

    /** 条款内容 / Clause content */
    private String content;

    /** 版本号 / Version number */
    private Integer version;

    /** 生效日期 / Effective date */
    private LocalDate effectiveDate;

    /** 状态: ACTIVE / INACTIVE / ARCHIVED */
    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
