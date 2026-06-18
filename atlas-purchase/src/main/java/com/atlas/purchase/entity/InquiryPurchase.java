package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 询比采购主表实体 / Inquiry-comparison procurement entity
 * <p>
 * 向至少3家供应商发送询价，一次性不可更改报价，比较后选择最优。 /
 * Send inquiries to at least 3 suppliers, one-time irrevocable quotes, select best after comparison.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("inquiry_purchase")
public class InquiryPurchase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 询价单号 / Inquiry number */
    private String inquiryNo;

    /** 关联采购单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 询价标题 / Inquiry title */
    private String title;

    /** 询价内容/规格要求 / Inquiry content / spec requirements */
    private String inquiryContent;

    /** 报价截止日期 / Quote submission deadline */
    private LocalDate inquiryDeadline;

    /** 最少询价供应商数 / Minimum supplier count */
    private Integer minSupplierCount;

    /** 状态: 0-草稿 1-询价中 2-报价结束 3-比较中 4-已定标 5-终止 / Status: 0-draft 1-inquiring 2-closed 3-comparing 4-awarded 5-terminated */
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
