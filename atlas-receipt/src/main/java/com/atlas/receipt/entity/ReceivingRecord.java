package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货质检联动记录实体 / Receiving & quality inspection linkage record entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("receiving_record")
public class ReceivingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 收货单号 / Receiving number */
    private String receiveNo;

    /** 关联ASN ID / Associated ASN ID */
    private Long asnId;

    /** 关联采购订单ID / Associated purchase order ID */
    private Long orderId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 收货仓库ID / Receiving warehouse ID */
    private Long warehouseId;

    /** 收货日期 / Receiving date */
    private LocalDateTime receiveDate;

    /** 收货人ID / Receiver ID */
    private Long receiverId;

    /** 是否已触发质检 / Whether inspection triggered */
    private Integer inspectionTriggered;

    /** 关联检验单ID / Associated inspection ID */
    private Long inspectionId;

    /** 质检结果 / Inspection result */
    private String inspectionResult;

    /** 关联收货确认单ID / Associated receipt confirmation ID */
    private Long receiptId;

    /** 状态：RECEIVED/INSPECTING/ACCEPTED/REJECTED / Status */
    private String status;

    /** 备注 / Remark */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
