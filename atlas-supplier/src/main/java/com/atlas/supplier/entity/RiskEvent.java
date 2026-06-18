package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风险事件实体 — 对应 risk_event 表 / Risk event entity — maps to risk_event table
 *
 * @author atlas
 */
@Data
@TableName("risk_event")
public class RiskEvent {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 风险类型: LEGAL / FINANCIAL / QUALITY / DELIVERY / CREDIT / Risk type */
    private String riskType;

    /** 风险等级: LOW / MEDIUM / HIGH / CRITICAL / Risk level */
    private String riskLevel;

    /** 事件描述 / Event description */
    private String eventDesc;

    /** 来源: EXTERNAL_API / INSPECTION / COMPLAINT / Source */
    private String eventSource;

    /** 发生时间 / Occurrence time */
    private LocalDateTime occurTime;

    /** 状态: 0待处理 1处理中 2已解决 3已关闭 / Status: 0=pending, 1=processing, 2=resolved, 3=closed */
    private Integer status;

    /** 处理结果 / Handle result */
    private String handleResult;

    /** 处理人ID / Handler ID */
    private Long handlerId;

    /** 处理人姓名 / Handler name */
    private String handlerName;

    /** 处理时间 / Handled time */
    private LocalDateTime handledAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
