package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商风险预警实体 — 对应 supplier_risk_alert 表 /
 * Supplier risk alert entity — maps to supplier_risk_alert table
 *
 * <p>记录供应商多来源风险：工商变更(BUSINESS_CHANGE)、司法风险(JUDICIAL)、
 * 经营异常(OPERATION)、财务风险(FINANCIAL)，支持风险录入/查询/标记解决。 /
 * Records multi-source supplier risks: business changes, judicial risks,
 * operational anomalies, financial risks, with entry/query/mark-resolved support.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("supplier_risk_alert")
public class SupplierRiskAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 风险类型: BUSINESS_CHANGE/JUDICIAL/OPERATION/FINANCIAL / Risk type */
    private String riskType;

    /** 风险等级: LOW/MEDIUM/HIGH/CRITICAL / Risk level */
    private String riskLevel;

    /** 风险描述 / Risk description */
    private String description;

    /** 数据来源: QICHACHA/TIANYANCHA/MANUAL / Data source */
    private String source;

    /** 预警时间 / Alert time */
    private LocalDateTime alertTime;

    /** 是否已解决: 0否 1是 / Resolved: 0=no, 1=yes */
    private Integer isResolved;

    /** 解决时间 / Resolved time */
    private LocalDateTime resolvedTime;

    /** 处理人ID / Handler ID */
    private Long handlerId;

    /** 处理人姓名 / Handler name */
    private String handlerName;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
