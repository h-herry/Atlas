package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警记录实体 — 对应 alert_record 表 / Alert record entity — maps to alert_record table
 *
 * @author atlas
 */
@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联规则ID / Associated rule ID */
    private Long ruleId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 预警类型 / Alert type */
    private String alertType;

    /** 预警级别 / Alert level */
    private String alertLevel;

    /** 预警标题 / Alert title */
    private String alertTitle;

    /** 预警详情 / Alert content */
    private String alertContent;

    /** 是否已读: 0未读 1已读 / Read status: 0=unread, 1=read */
    private Integer isRead;

    /** 是否已处理: 0未处理 1已处理 / Handled: 0=no, 1=yes */
    private Integer isHandled;

    /** 处理人ID / Handler ID */
    private Long handlerId;

    /** 处理时间 / Handle time */
    private LocalDateTime handleTime;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
