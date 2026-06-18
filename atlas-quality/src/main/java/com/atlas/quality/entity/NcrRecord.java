package com.atlas.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 不合格品处理记录（NCR）实体 / Non-Conformance Report (NCR) entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("ncr_record")
public class NcrRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** NCR单号 / NCR number */
    private String ncrNo;

    /** 关联检验单ID / Associated inspection ID */
    private Long inspectId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 物料名称 / Material name */
    private String materialName;

    /** 生产批次号 / Production batch number */
    private String batchNo;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 缺陷类型：DIMENSION/APPEARANCE/FUNCTION/MATERIAL/PACKAGING/OTHER / Defect type */
    private String defectType;

    /** 缺陷描述 / Defect description */
    private String defectDescription;

    /** 不合格数量 / Defect quantity */
    private BigDecimal defectQty;

    /** 严重程度：CRITICAL/MAJOR/MINOR / Severity */
    private String defectSeverity;

    /** 处置方式：ACCEPT/CONCESSION/SORT/RETURN/SCRAP / Disposition */
    private String disposition;

    /** 处置人ID / Disposition by */
    private Long dispositionBy;

    /** 处置时间 / Disposition time */
    private LocalDateTime dispositionAt;

    /** 处置理由 / Disposition reason */
    private String dispositionReason;

    /** 纠正措施 / Corrective action */
    private String correctiveAction;

    /** 是否闭环 / Whether closed */
    private Integer closed;

    /** 闭环时间 / Closed time */
    private LocalDateTime closedAt;

    /** 创建人 / Created by */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
