package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞价采购主表实体 / Auction purchase main table entity
 * <p>
 * 反向拍卖模式：供应商多轮降价竞价，支持自动延时。 /
 * Reverse auction mode: suppliers compete via multi-round price reduction, supports auto-extension.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("auction_purchase")
public class AuctionPurchase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 竞价编号 / Auction number */
    private String auctionNo;

    /** 关联采购单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 竞价标题 / Auction title */
    private String title;

    /** 竞价内容/技术要求 / Auction content / technical requirements */
    private String auctionContent;

    /** 起拍价/最高限价 / Starting price / maximum price */
    private BigDecimal startPrice;

    /** 最小降价幅度 / Minimum bid decrement */
    private BigDecimal minDecrement;

    /** 竞价开始时间 / Auction start time */
    private LocalDateTime startTime;

    /** 竞价结束时间 / Auction end time */
    private LocalDateTime endTime;

    /** 竞价类型: REVERSE反向竞价 / FORWARD正向竞价 / Auction type: REVERSE / FORWARD */
    private String auctionType;

    /** 最后时刻是否自动延时 / Whether to auto-extend at last moment */
    private Integer autoExtend;

    /** 自动延时分钟数 / Auto-extension minutes */
    private Integer extendMinutes;

    /** 状态: 0-准备中 1-竞价中 2-已结束 3-已定标 4-终止 / Status: 0-preparing 1-bidding 2-ended 3-awarded 4-terminated */
    private Integer status;

    /** 成交供应商ID / Winning supplier ID */
    private Long winnerSupplierId;

    /** 成交金额 / Winning amount */
    private BigDecimal winnerAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
