package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞争性磋商会话实体 / Competitive consultation session entity
 * <p>
 * 采用结构化流程 + 综合评分法，适用于科技创新、政府购买服务等复杂项目。 /
 * Structured process + comprehensive scoring method, suitable for complex projects like tech innovation and government service procurement.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("consultation_session")
public class ConsultationSession {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联采购订单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 磋商编号 / Consultation number */
    private String consultationNo;

    /** 磋商标题 / Consultation title */
    private String title;

    /** 磋商内容/需求说明 / Consultation content / requirements description */
    private String content;

    /** 邀请供应商ID（JSON数组） / Invited supplier IDs (JSON array) */
    private String invitedSupplierIds;

    /** 综合评分规则（JSON: 各维度权重） / Scoring rules (JSON: dimension weights) */
    private String scoringRules;

    /** 状态: 0-草稿 1-公告期 2-响应文件提交 3-磋商中 4-最终报价 5-综合评审 6-定标 7-终止 / Status: 0-draft 1-announcement 2-submission 3-consulting 4-final quote 5-review 6-awarded 7-terminated */
    private Integer status;

    /** 成交供应商ID / Winning supplier ID */
    private Long winnerSupplierId;

    /** 成交金额 / Winning amount */
    private BigDecimal winnerAmount;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
