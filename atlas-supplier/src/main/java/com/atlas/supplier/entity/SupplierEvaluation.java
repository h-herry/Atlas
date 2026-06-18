package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商绩效考核主表实体 — 对应 supplier_evaluation 表 / Supplier performance evaluation master entity — maps to supplier_evaluation table
 *
 * @author atlas
 */
@Data
@TableName("supplier_evaluation")
public class SupplierEvaluation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 评估模板ID / Template ID */
    private Long templateId;

    /** 评估周期: 2026-Q1 / Evaluation period */
    private String evalPeriod;

    /** 评估类型: 1季度 2年度 3专项 / Evaluation type: 1=quarterly, 2=annual, 3=special */
    private Integer evalType;

    /** 质量得分 / Quality score */
    private BigDecimal qualityScore;

    /** 交付得分 / Delivery score */
    private BigDecimal deliveryScore;

    /** 成本得分 / Cost score */
    private BigDecimal costScore;

    /** 服务得分 / Service score */
    private BigDecimal serviceScore;

    /** 综合得分 / Total score */
    private BigDecimal totalScore;

    /** 等级: A/B/C/D / Grade */
    private String evalLevel;

    /** 整改要求 / Improvement note */
    private String improvementNote;

    /** 整改截止日期 / Improvement deadline */
    private LocalDate improvementDeadline;

    /** 评估人ID / Evaluator ID */
    private Long evaluatorId;

    /** 评估人姓名 / Evaluator name */
    private String evaluatorName;

    /** 状态: 0草稿 1已发布 2供应商确认 3整改中 4整改完成 5关闭 / Status: 0=draft, 1=published, 2=supplier confirmed, 3=improving, 4=improved, 5=closed */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
