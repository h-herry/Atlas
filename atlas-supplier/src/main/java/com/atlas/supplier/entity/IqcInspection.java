package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 来料检验 IQC 实体 / Incoming Quality Control (IQC) entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("iqc_inspection")
public class IqcInspection implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 检验单号 / Inspection number */
    private String inspectionNo;

    /** 关联发货单ID / Delivery order ID */
    private Long deliveryId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 生产批次号 / Production batch number */
    private String batchNo;

    /** 报检数量 / Reported inspection quantity */
    private BigDecimal inspectionQty;

    /** 抽样数量 / Sample quantity */
    private BigDecimal sampleQty;

    /** 合格数量 / Qualified quantity */
    private BigDecimal qualifiedQty;

    /** 不合格数量 / Defective quantity */
    private BigDecimal defectiveQty;

    /** 检验标准 / Inspection standard */
    private String inspectionStandard;

    /** 检验结果: PASS合格 / REJECT退货 / REWORK返工 / ACCEPT让步接收 / Result: PASS/REJECT/REWORK/ACCEPT */
    private String result;

    /** 检验员ID / Inspector ID */
    private Long inspectorId;

    /** 检验时间 / Inspected time */
    private LocalDateTime inspectedAt;

    /** 备注 / Remark */
    private String remark;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
