package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商绩效评分卡主表实体 — 对应 supplier_scorecard 表 /
 * Supplier performance scorecard master entity — maps to supplier_scorecard table
 *
 * <p>QCD 四维评分模型：质量 30% + 成本 25% + 交付 25% + 服务 20%，满分 100。 /
 * QCD 4-dimension scoring model: Quality 30% + Cost 25% + Delivery 25% + Service 20%, max 100.
 * 评分结果联动供应商等级和配额分配。 / Score results linked to supplier grade and quota allocation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("supplier_scorecard")
public class SupplierScorecard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 评分周期: 2026-Q2 / Scoring period */
    private String period;

    /** 质量得分（满分30） / Quality score (max 30) */
    private BigDecimal qualityScore;

    /** 成本得分（满分25） / Cost score (max 25) */
    private BigDecimal costScore;

    /** 交付得分（满分25） / Delivery score (max 25) */
    private BigDecimal deliveryScore;

    /** 服务得分（满分20） / Service score (max 20) */
    private BigDecimal serviceScore;

    /** 综合得分（满分100） / Total score (max 100) */
    private BigDecimal totalScore;

    /** 评级: A/B/C/D / Grade */
    private String grade;

    /** 评分人ID / Assessor ID */
    private Long assessorId;

    /** 评分人姓名 / Assessor name */
    private String assessorName;

    /** 备注 / Remark */
    private String remark;

    /** 状态: 0草稿 1已发布 2已确认 / Status: 0=draft, 1=published, 2=confirmed */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
