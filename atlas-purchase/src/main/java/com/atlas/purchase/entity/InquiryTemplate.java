package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 询价模板实体 — 对应 inquiry_template 表 /
 * Inquiry template entity — maps to inquiry_template table
 *
 * <p>询价模板关联物料分类，自动加载对应属性字段，支持交期/质量资质是否必填、
 * 价格明细是否启用等配置，模板版本管理，修改历史可追溯。 /
 * RFQ templates linked to material categories, auto-load corresponding attribute fields,
 * supports mandatory delivery/quality cert config, price breakdown toggle,
 * template versioning with change history traceability.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("inquiry_template")
public class InquiryTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板编号 / Template number */
    private String templateId;

    /** 模板名称 / Template name */
    private String name;

    /** 关联物料分类ID / Associated material category ID */
    private Long categoryId;

    /** 交期是否必填: 0否 1是 / Delivery required: 0=no, 1=yes */
    private Integer deliveryRequired;

    /** 质量资质是否必填: 0否 1是 / Quality cert required: 0=no, 1=yes */
    private Integer qualityRequired;

    /** 是否启用价格明细: 0否 1是 / Price breakdown enabled: 0=no, 1=yes */
    private Integer priceBreakdownEnabled;

    /** 关联的属性模板字段ID列表（JSON数组） / Linked attribute template field IDs (JSON array) */
    private String attrFieldIds;

    /** 状态: 1启用 0停用 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
