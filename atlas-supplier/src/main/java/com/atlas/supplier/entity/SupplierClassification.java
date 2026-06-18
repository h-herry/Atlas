package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商分级实体 — 对应 supplier_classification 表 /
 * Supplier classification entity — maps to supplier_classification table
 *
 * <p>四级分级：战略(STRATEGIC) / 核心(CORE) / 一般(GENERAL) / 潜在(POTENTIAL)，
 * 基于绩效评分卡自动升降级，升降级记录留痕，支持人工复议。 /
 * Four tiers: STRATEGIC / CORE / GENERAL / POTENTIAL,
 * auto promotion/demotion based on scorecard, with audit trail and manual review option.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("supplier_classification")
public class SupplierClassification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 分级: STRATEGIC/CORE/GENERAL/POTENTIAL / Grade */
    private String grade;

    /** 评定日期 / Assessment date */
    private LocalDate assessedDate;

    /** 有效期至 / Valid until */
    private LocalDate validUntil;

    /** 评定人ID / Assessor ID */
    private Long assessorId;

    /** 评定人姓名 / Assessor name */
    private String assessorName;

    /** 评定依据 / Assessment rationale */
    private String reason;

    /** 前次分级 / Previous grade */
    private String prevGrade;

    /** 状态: 1有效 0失效 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
