package com.atlas.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 来料批次追溯实体 — 对应 lot_trace /
 * Incoming lot trace entity — maps to lot_trace
 * <p>
 * 汽车零部件制造业要求严格批次管理：每批来料记录供应商批次号+收货日期，
 * 支持正向追溯（按批次号查所有信息）和反向追溯（按物料查所有关联批次）。 /
 * Automotive parts manufacturing requires strict lot management: each incoming lot records
 * supplier lot number + receive date; supports forward traceability (by lot number)
 * and reverse traceability (by material to all related lots).
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("lot_trace")
public class LotTrace {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 批次号 / Lot number */
    private String lotNo;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 关联采购订单ID / Related purchase order ID */
    private Long orderId;

    /** 收货日期 / Receive date */
    private LocalDate receiveDate;

    /** 批次数量 / Lot quantity */
    private BigDecimal quantity;

    /** 状态: RECEIVED/INSPECTING/QUALIFIED/REJECTED/CONSUMED */
    private String status;

    /** 检验结果: PASS/FAIL / Inspection result */
    private String inspectionResult;

    /** 检验日期 / Inspection date */
    private LocalDateTime inspectionDate;

    /** 不合格原因 / Rejection reason */
    private String rejectionReason;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== 状态常量 / Status Constants ====================

    public static final String STATUS_RECEIVED   = "RECEIVED";
    public static final String STATUS_INSPECTING = "INSPECTING";
    public static final String STATUS_QUALIFIED  = "QUALIFIED";
    public static final String STATUS_REJECTED   = "REJECTED";
    public static final String STATUS_CONSUMED   = "CONSUMED";

    // ==================== 检验结果常量 / Inspection Result Constants ====================

    public static final String INSPECTION_PASS = "PASS";
    public static final String INSPECTION_FAIL = "FAIL";

    /**
     * 生成标准批次号：供应商批次号+收货日期 / Generate standard lot number: supplier batch + receive date
     *
     * @param supplierBatch 供应商原始批次号 / Supplier original batch number
     * @param receiveDate   收货日期 / Receive date
     * @return 标准批次号(如 ABC123-20260618) / Standard lot number (e.g. ABC123-20260618)
     */
    public static String generateLotNo(String supplierBatch, LocalDate receiveDate) {
        return supplierBatch + "-" + receiveDate.toString().replace("-", "");
    }
}
