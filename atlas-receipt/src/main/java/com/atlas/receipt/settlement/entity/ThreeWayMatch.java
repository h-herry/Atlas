package com.atlas.receipt.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 三单匹配结果实体 / Three-way match result entity
 * <p>
 * 匹配维度：PO 采购订单 vs 收货单 vs 发票 /
 * Match dimensions: PO vs Receipt vs Invoice
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Data
@TableName("three_way_match")
public class ThreeWayMatch {

    /** 匹配ID（雪花算法） / Match ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联结算单ID / Related settlement ID */
    private Long settlementId;

    /** 采购订单ID / Purchase order ID */
    private Long poId;

    /** 收货单ID / Receipt ID */
    private Long receiptId;

    /** 发票号 / Invoice number */
    private String invoiceNo;

    /** 匹配状态：MATCHED/MISMATCH/PENDING */
    private String matchStatus;

    /** 差异明细（JSON格式，记录具体差异项） / Difference details (JSON, records specific discrepancies) */
    private String diffDetails;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
