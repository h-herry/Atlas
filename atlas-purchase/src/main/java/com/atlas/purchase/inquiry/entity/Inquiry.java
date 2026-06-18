package com.atlas.purchase.inquiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 询价单实体 / Inquiry entity
 *
 * <p>记录一次完整的询价流程：从草稿到发布、报价中、比价、关闭。 /
 * Records a complete inquiry workflow: from draft to published, quoting, comparison, closed closed.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("inquiry")
public class Inquiry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 询价标题 / Inquiry title */
    private String title;

    /** 状态: DRAFT / PUBLISHED / QUOTING / COMPARED / CLOSED */
    private String status;

    /** 发布时间 / Publish time */
    private LocalDateTime publishTime;

    /** 报价截止时间 / Quote close time */
    private LocalDateTime closeTime;

    /** 创建人ID / Creator ID */
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
