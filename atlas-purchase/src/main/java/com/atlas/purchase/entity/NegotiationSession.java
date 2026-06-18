package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞争性谈判会话实体 / Competitive negotiation session entity
 * <p>
 * 与2家及以上供应商分别进行多轮谈判，采用最低价评审法定标。 /
 * Multi-round negotiation with 2+ suppliers respectively, awarded via lowest-price evaluation method.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("negotiation_session")
public class NegotiationSession {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联采购订单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 谈判编号 / Negotiation number */
    private String negotiationNo;

    /** 谈判标题 / Negotiation title */
    private String title;

    /** 谈判内容/技术要求 / Negotiation content / technical requirements */
    private String content;

    /** 邀请供应商ID（JSON数组，至少2家） / Invited supplier IDs (JSON array, at least 2) */
    private String invitedSupplierIds;

    /** 评审方法: LOWEST_PRICE / Evaluation method: LOWEST_PRICE */
    private String negotiationMethod;

    /** 状态: 0-草稿 1-已发布 2-谈判中 3-报价收集完成 4-评审中 5-定标 6-终止 / Status: 0-draft 1-published 2-negotiating 3-quotes collected 4-reviewing 5-awarded 6-terminated */
    private Integer status;

    /** 成交供应商ID / Winning supplier ID */
    private Long winnerSupplierId;

    /** 成交金额 / Winning amount */
    private BigDecimal winnerAmount;

    /** 谈判轮次 / Round count */
    private Integer roundCount;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
