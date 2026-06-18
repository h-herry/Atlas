package com.atlas.purchase.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JIT交货排程实体 — 对应 jit_delivery_schedule /
 * JIT delivery schedule entity — maps to jit_delivery_schedule
 * <p>
 * 汽车行业 JIT 模式下，订单发布后系统计算交货窗口，
 * 供应商必须在窗口内确认，超时自动标记 MISSED 并触发预警。 /
 * In automotive JIT mode, the system calculates delivery window after order release;
 * supplier must confirm within the window; auto-mark MISSED on timeout and trigger alert.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("jit_delivery_schedule")
public class JitDeliverySchedule {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联订单ID / Related order ID */
    private Long orderId;

    /** 交货日期 / Delivery date */
    private LocalDate deliveryDate;

    /** 交货窗口开始时间 / Delivery window start */
    private LocalTime windowStart;

    /** 交货窗口结束时间 / Delivery window end */
    private LocalTime windowEnd;

    /** 状态: PENDING-待确认 / CONFIRMED-已确认 / MISSED-超时未确认 */
    private String status;

    /** 供应商确认时间 / Supplier confirmation time */
    private LocalDateTime confirmTime;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== 状态常量 / Status Constants ====================

    /** 待确认 / Pending confirmation */
    public static final String STATUS_PENDING = "PENDING";

    /** 已确认 / Confirmed */
    public static final String STATUS_CONFIRMED = "CONFIRMED";

    /** 超时未确认 / Missed deadline */
    public static final String STATUS_MISSED = "MISSED";
}
