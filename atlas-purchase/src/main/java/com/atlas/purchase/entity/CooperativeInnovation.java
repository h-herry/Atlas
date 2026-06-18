package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合作创新采购实体 / Cooperative innovation procurement entity
 * <p>
 * 邀请供应商合作研发创新产品，共担风险并承诺购买成果。 /
 * Invites suppliers for cooperative R&D of innovative products, sharing risks with purchase commitment.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("cooperative_innovation")
public class CooperativeInnovation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创新采购编号 / Innovation procurement number */
    private String innovationNo;

    /** 关联采购单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 创新采购标题 / Innovation procurement title */
    private String title;

    /** 研发内容 / R&D content */
    private String rdContent;

    /** 研发预算 / R&D budget */
    private BigDecimal rdBudget;

    /** 研发周期 / R&D cycle */
    private String rdCycle;

    /** 知识产权归属: BUYER/SUPPLIER/SHARED / IP ownership: BUYER / SUPPLIER / SHARED */
    private String ipOwnership;

    /** 研发阶段数 / Number of R&D stages */
    private Integer stageCount;

    /** 状态: 0-征集中 1-评审中 2-合作中 3-验收中 4-已完成 5-终止 / Status: 0-collecting 1-reviewing 2-cooperating 3-acceptance 4-completed 5-terminated */
    private Integer status;

    /** 合作供应商ID / Partner supplier ID */
    private Long partnerSupplierId;

    /** 各阶段进度 / Stage progress (JSON) */
    private String stageProgress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
