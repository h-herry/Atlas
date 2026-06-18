package com.atlas.message.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息模板实体 — 对应 msg_template 表 /
 * Message template entity — maps to msg_template table
 *
 * <p>预置消息模板，支持占位符替换，用于在业务节点生成标准化推送内容 /
 * Predefined message templates with placeholder substitution for generating standardized push content at business nodes</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("msg_template")
public class MessageTemplateModel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板编码（唯一） / Template code (unique) */
    private String templateCode;

    /** 标题模板（支持占位符 {supplierName} {orderNo} 等） / Title template (supports placeholders) */
    private String titleTemplate;

    /** 内容模板 / Content template */
    private String contentTemplate;

    /** 消息类型 / Message type */
    private String type;

    /** 是否启用: 1 启用 / 0 禁用 / Is active: 1 enabled / 0 disabled */
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
