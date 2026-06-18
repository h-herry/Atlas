package com.atlas.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PPAP提交主实体 — 对应 ppap_submission /
 * PPAP submission master entity — maps to ppap_submission
 * <p>
 * PPAP (Production Part Approval Process) 是汽车行业标准的新零件批准流程，
 * 供应商必须提交 18 项要素文件，经由采购/质量工程师逐项审核。 /
 * PPAP is the automotive industry standard new part approval process;
 * supplier must submit 18 element documents for item-by-item review by procurement/quality engineers.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("ppap_submission")
public class PpapSubmission {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** PPAP等级(1~5) / PPAP level (1~5) */
    private Integer ppapLevel;

    /** 提交日期 / Submission date */
    private LocalDate submissionDate;

    /** 状态: PENDING/SUBMITTED/UNDER_REVIEW/INTERIM_APPROVED/FULLY_APPROVED/REJECTED */
    private String status;

    /** 批准日期 / Approval date */
    private LocalDate approvalDate;

    /** 批准人 / Approved by */
    private Long approvedBy;

    /** 关联项目编码 / Related project code */
    private String projectCode;

    /** SOP节点日期 / SOP milestone date */
    private LocalDate sopDate;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== 状态常量 / Status Constants ====================

    public static final String STATUS_PENDING          = "PENDING";
    public static final String STATUS_SUBMITTED        = "SUBMITTED";
    public static final String STATUS_UNDER_REVIEW     = "UNDER_REVIEW";
    public static final String STATUS_INTERIM_APPROVED = "INTERIM_APPROVED";
    public static final String STATUS_FULLY_APPROVED   = "FULLY_APPROVED";
    public static final String STATUS_REJECTED         = "REJECTED";
}
