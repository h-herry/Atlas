package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单变更明细实体 / Order change detail entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("order_change_detail")
public class OrderChangeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联变更单ID / Associated change ID */
    private Long changeId;

    /** 变更字段名 / Changed field name */
    private String fieldName;

    /** 字段显示名 / Field display label */
    private String fieldLabel;

    /** 变更前值 / Old value */
    private String oldValue;

    /** 变更后值 / New value */
    private String newValue;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
