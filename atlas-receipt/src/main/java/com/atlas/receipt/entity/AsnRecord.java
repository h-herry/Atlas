package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ASN 预先发货通知实体 / ASN (Advanced Shipping Notice) record entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("asn_record")
public class AsnRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** ASN单号 / ASN number */
    private String asnNo;

    /** 关联采购订单ID / Associated purchase order ID */
    private Long orderId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 预计到货日 / Expected arrival date */
    private LocalDate expectedArrivalDate;

    /** 实际发货日 / Actual ship date */
    private LocalDate shipDate;

    /** 承运商 / Carrier name */
    private String carrier;

    /** 物流单号 / Tracking number */
    private String trackingNo;

    /** 车牌号 / Vehicle plate number */
    private String vehicleNo;

    /** 司机姓名 / Driver name */
    private String driverName;

    /** 司机电话 / Driver phone */
    private String driverPhone;

    /** 状态：CREATED/IN_TRANSIT/ARRIVED/RECEIVED/CANCELLED / Status */
    private String status;

    /** 备注 / Remark */
    private String remark;

    /** 创建人 / Created by */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
