package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商改善跟踪闭环实体 — 对应 supplier_improvement 表 /
 * Supplier improvement (corrective action closed-loop) entity — maps to supplier_improvement table
 *
 * <p>整改闭环流程：OPEN(开启) → IN_PROGRESS(进行中) → VERIFIED(已验证) → CLOSED(关闭)。 /
 * Correction workflow: OPEN → IN_PROGRESS → VERIFIED → CLOSED.
 * 支持根因分析、纠正措施记录、验证闭环，超时自动升级告警。 /
 * Supports root cause analysis, corrective action recording, verification and closure, with auto-escalation on timeout.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("supplier_improvement")
public class SupplierImprovement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 问题类型: QUALITY/DELIVERY/PERFORMANCE/OTHER / Issue type */
    private String issueType;

    /** 整改标题 / Improvement title */
    private String title;

    /** 问题描述 / Issue description */
    private String description;

    /** 根因分析 / Root cause analysis */
    private String rootCause;

    /** 纠正措施 / Corrective action */
    private String correctiveAction;

    /** 验证人ID / Verifier ID */
    private Long verifierId;

    /** 验证人姓名 / Verifier name */
    private String verifierName;

    /** 整改截止日期 / Improvement deadline */
    private LocalDate deadline;

    /** 状态: OPEN→IN_PROGRESS→VERIFIED→CLOSED / Status flow */
    private String status;

    /** 验证时间 / Verified time */
    private LocalDateTime verifiedAt;

    /** 关闭时间 / Closed time */
    private LocalDateTime closedAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
