package com.atlas.purchase.inquiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞价场次实体 / Bidding session entity
 *
 * <p>关联一个询价单的竞价场次，支持公开竞价(OPEN)和密封竞价(SEALED)两种模式。 /
 * A bidding session linked to an inquiry, supporting OPEN and SEALED bidding types.
 * Redis SortedSet key: bidding:{sessionId}</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("bidding_session")
public class BiddingSession {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联询价单ID / Associated inquiry ID */
    private Long inquiryId;

    /** 竞价模式: OPEN / SEALED */
    private String biddingType;

    /** 竞价开始时间 / Bidding start time */
    private LocalDateTime startTime;

    /** 竞价结束时间 / Bidding end time */
    private LocalDateTime endTime;

    /** 底价 / Floor price */
    private BigDecimal floorPrice;

    /** 最小加价幅度 / Minimum bid step */
    private BigDecimal minStep;

    /** 场次状态: PENDING / ACTIVE / CLOSED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
