package com.atlas.purchase.inquiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询价行项目实体 / Inquiry item entity
 *
 * <p>一个询价单包含多个行项目，每个行项目对应一种物料的需求明细。 /
 * An inquiry contains multiple items, each representing demand details for one material.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("inquiry_item")
public class InquiryItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联询价单ID / Associated inquiry ID */
    private Long inquiryId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 需求数量 / Required quantity */
    private BigDecimal quantity;

    /** 规格要求 / Specification requirement */
    private String spec;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
