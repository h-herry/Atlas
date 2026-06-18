package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商整改跟踪实体 — 对应 supplier_rectification 表 / Supplier rectification tracking entity — maps to supplier_rectification table
 *
 * <p>支持 8D 报告整改跟踪：创建整改单 → 供应商提交方案 → 复评闭环。
 * 状态流程：0待整改 → 1方案已提交 → 2整改中 → 3待复评 → 4已完成（5逾期） /
 * Supports 8D report rectification tracking: create rectification order → supplier submits plan → re-evaluation closure.
 * Status flow: 0=pending → 1=plan submitted → 2=in progress → 3=pending review → 4=completed (5=overdue)</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("supplier_rectification")
public class SupplierRectification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 问题类型: QUALITY/DELIVERY/SERVICE/CERT / Issue type */
    private String issueType;

    /** 问题描述 / Issue description */
    private String issueDesc;

    /** 严重程度: MAJOR/MINOR/CRITICAL / Severity */
    private String severity;

    /** 整改截止日期 / Rectification deadline */
    private LocalDate deadline;

    /** 供应商提交的整改方案 / Rectification plan submitted by supplier */
    private String rectificationPlan;

    /** 整改证据附件URL / Evidence attachment URL */
    private String evidenceUrl;

    /** 状态: 0待整改 1方案已提交 2整改中 3待复评 4已完成 5逾期 / Status */
    private Integer status;

    /** 审核人ID / Auditor ID */
    private Long auditorId;

    /** 复评结果: PASS/FAIL/EXTEND / Review result */
    private String result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
