package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞价大厅实体 / Bidding hall entity
 * <p>
 * 支持英式(ENGLISH)/荷兰式(DUTCH)/日式(JAPANESE)三种竞价模式，
 * 可配置隐藏供应商身份、公开排名、最后报价自动延长等策略。 /
 * Supports English/Dutch/Japanese three auction modes,
 * configurable identity-hiding, public ranking, auto-extension on last bid.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("bidding_hall")
public class BiddingHall {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联竞价单ID / Associated auction ID */
    private Long auctionId;

    /** 大厅状态: 0待开启 1进行中 2已结束 / Hall status: 0-pending 1-active 2-ended */
    private Integer hallStatus;

    /** 竞价模式: ENGLISH英式 / DUTCH荷兰式 / JAPANESE日式 / Bidding style: ENGLISH / DUTCH / JAPANESE */
    private String biddingStyle;

    /** 是否隐藏供应商身份 / Whether to hide supplier identity */
    private Integer identityHidden;

    /** 是否公开排名 / Whether rankings are public */
    private Integer rankPublic;

    /** 最后报价自动延长时间(秒) / Auto-extension duration on last bid (seconds) */
    private Integer autoExtendSeconds;

    /** 竞价开始时间 / Bidding start time */
    private LocalDateTime startTime;

    /** 竞价结束时间 / Bidding end time */
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
