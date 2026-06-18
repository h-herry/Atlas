package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 框架协议主表实体 / Framework agreement main table entity
 * <p>
 * 公开程序入围→签订框架协议→实际需求时二次确定成交方，适用于多频次小额度采购。 /
 * Open process shortlisting → sign framework agreement → second-stage winner selection when demand arises,
 * suitable for frequent small-amount procurement.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("framework_agreement")
public class FrameworkAgreement {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 协议编号 / Agreement number */
    private String agreementNo;

    /** 协议名称 / Agreement name */
    private String agreementName;

    /** 采购类别 / Procurement type */
    private Integer procurementType;

    /** 预估总金额 / Estimated total amount */
    private BigDecimal estimatedTotalAmount;

    /** 有效期起 / Valid from */
    private LocalDate validFrom;

    /** 有效期止 / Valid to */
    private LocalDate validTo;

    /** 入围方式: 1-公开招标 2-邀请招标 3-竞争性磋商 / Shortlisting method: 1-open bidding 2-invited bidding 3-competitive consultation */
    private Integer supplierSelectionMethod;

    /** 状态: 0-草稿 1-入围征集中 2-评审中 3-入围确定 4-协议生效 5-执行中 6-到期 7-终止 / Status: 0-draft 1-collecting 2-reviewing 3-shortlisted 4-effective 5-executing 6-expired 7-terminated */
    private Integer status;

    /** 最大入围供应商数 / Max shortlisted supplier count */
    private Integer maxSupplierCount;

    /** 实际入围供应商数 / Actual shortlisted supplier count */
    private Integer actualSupplierCount;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
