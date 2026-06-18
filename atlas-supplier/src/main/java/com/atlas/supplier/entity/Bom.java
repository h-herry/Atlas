package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BOM（物料清单）主表实体 / BOM (Bill of Materials) master entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("bom")
public class Bom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** BOM编号 / BOM code */
    private String bomCode;

    /** 产品名称 / Product name */
    private String productName;

    /** 版本号 / Version */
    private String version;

    /** 状态: 0编辑中 1已发布 2已归档 / Status: 0=draft, 1=published, 2=archived */
    private Integer status;

    /** 创建人ID / Creator ID */
    private Long createdBy;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
