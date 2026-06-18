package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 框架协议二次订单实体 / Framework agreement secondary order entity
 * <p>
 * 框架协议执行中根据实际需求产生的二次竞争订单。 /
 * Secondary competitive order generated from actual demand during framework agreement execution.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("framework_order")
public class FrameworkOrder {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联框架协议ID / Associated framework agreement ID */
    private Long agreementId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 二次订单号 / Secondary order number */
    private String orderNo;

    /** 订单金额 / Order amount */
    private BigDecimal orderAmount;

    /** 采购内容 / Order content */
    private String orderContent;

    /** 成交方式: 1-直接选定 2-二次竞价 3-比例分配 / Selection method: 1-direct 2-second bidding 3-proportional allocation */
    private Integer selectMethod;

    /** 状态: 0-草稿 1-已下单 2-已确认 3-履约中 4-完成 / Status: 0-draft 1-ordered 2-confirmed 3-fulfilling 4-completed */
    private Integer status;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
