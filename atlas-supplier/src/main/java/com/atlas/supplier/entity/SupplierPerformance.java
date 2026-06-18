package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商绩效汇总实体 / Supplier performance summary entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("supplier_performance")
public class SupplierPerformance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 统计周期（如 2026-Q2） / Statistics period (e.g. 2026-Q2) */
    private String period;

    /** 交付准时率(%) / On-time delivery rate (%) */
    private BigDecimal deliveryOnTimeRate;

    /** 质量合格率(%) / Quality pass rate (%) */
    private BigDecimal qualityPassRate;

    /** 平均响应时长(小时) / Average response hours */
    private BigDecimal avgResponseHours;

    /** 投诉次数 / Complaint count */
    private Integer complaintCount;

    /** 综合得分 / Overall score */
    private BigDecimal score;

    /** 评级: A/B/C/D / Grade */
    private String grade;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
