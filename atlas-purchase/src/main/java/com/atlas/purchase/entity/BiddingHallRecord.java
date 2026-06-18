package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞价报价记录实体 / Bidding hall record entity
 * <p>
 * 记录每次竞价报价的供应商、金额、时间（精确到毫秒）， /
 * Records each bidding quote: supplier, amount, time (millisecond precision),
 * 用于实时排名刷新（误差 &lt; 0.05s）。 / Used for real-time ranking refresh (error &lt; 0.05s).</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("bidding_hall_record")
public class BiddingHallRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 大厅ID / Hall ID */
    private Long hallId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 报价金额 / Bid amount */
    private BigDecimal bidAmount;

    /** 当前排名 / Current rank */
    private Integer rankPosition;

    /** 报价时间（毫秒精度） / Bid time (millisecond precision) */
    private LocalDateTime bidTime;
}
