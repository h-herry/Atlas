package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对账单实体 — 对应 reconciliation 表 / Reconciliation entity — maps to reconciliation table
 *
 * @author atlas
 */
@Data
@TableName("reconciliation")
public class Reconciliation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 对账单号 / Reconciliation number */
    private String reconcilNo;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 对账周期起 / Period start */
    private LocalDate periodStart;

    /** 对账周期止 / Period end */
    private LocalDate periodEnd;

    /** 采购总额 / Total purchase amount */
    private BigDecimal purchaseTotal;

    /** 退货总额 / Total return amount */
    private BigDecimal returnTotal;

    /** 应付净额 / Net payable amount */
    private BigDecimal netAmount;

    /** 状态: 0待确认 1供应商确认 2采购方确认 3已开票 4已付款 / Status: 0=pending, 1=supplier confirmed, 2=buyer confirmed, 3=invoiced, 4=paid */
    private Integer status;

    /** 确认人 / Confirmed by */
    private String confirmedBy;

    /** 确认时间 / Confirmed time */
    private LocalDateTime confirmedAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
