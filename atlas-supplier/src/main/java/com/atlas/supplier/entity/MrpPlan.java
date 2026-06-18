package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MRP 需求计划主表实体 / MRP demand plan master entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("mrp_plan")
public class MrpPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 计划编号 / Plan number */
    private String planNo;

    /** 计划类型: MPS主计划 / MRP物料计划 / Plan type: MPS/MRP */
    private String planType;

    /** 计划周期开始 / Period start */
    private LocalDate periodStart;

    /** 计划周期结束 / Period end */
    private LocalDate periodEnd;

    /** 状态: 0草稿 1已计算 2已确认 3已下发 / Status: 0=draft, 1=calculated, 2=confirmed, 3=issued */
    private Integer status;

    /** 创建人ID / Creator ID */
    private Long createdBy;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
