package com.atlas.purchase.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应付账款账龄分析实体 — 按供应商汇总应付余额+账龄分布 /
 * Accounts payable aging analysis entity — aggregated by supplier with aging distribution
 *
 * <p>账龄分段: 0-30天 / 31-60天 / 61-90天 / 91天以上。超90天自动标记预警 /
 * Aging buckets: 0-30 / 31-60 / 61-90 / 91+ days. Auto-flag alert for 90+ days</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("aging_analysis")
public class AgingAnalysis {

    @TableId(type = IdType.ASSIGN_ID)
    private Long agingId;

    private Long supplierId;
    private String supplierName;
    private LocalDate asOfDate;

    /** 应付总金额 / Total payable amount */
    private BigDecimal totalPayable;

    /** 账龄区间 / Aging buckets */
    private BigDecimal aging030;
    private BigDecimal aging3160;
    private BigDecimal aging6190;
    private BigDecimal aging91Plus;

    /** 超90天预警标记: 0正常 1预警 / Over 90-day alert: 0-normal 1-alert */
    private Integer overdueFlag;

    private LocalDateTime calculatedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
