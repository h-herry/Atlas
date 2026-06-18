package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警规则实体 — 对应 alert_rule 表 / Alert rule entity — maps to alert_rule table
 *
 * @author atlas
 */
@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 规则名称 / Rule name */
    private String ruleName;

    /** 规则类型: QUALIFICATION_EXPIRE / DELIVERY_DELAY / QUALITY_DEFECT / SCORE_FALL / Rule type */
    private String ruleType;

    /** 触发条件（JSON） / Trigger condition (JSON) */
    private String triggerCondition;

    /** 预警级别 / Alert level */
    private String alertLevel;

    /** 通知人员ID（JSON数组） / Notified user IDs (JSON array) */
    private String notifyUsers;

    /** 是否启用 / Enabled */
    private Integer isEnabled;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
