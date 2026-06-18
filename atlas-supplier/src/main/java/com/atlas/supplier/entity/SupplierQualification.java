package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商资质实体 / Supplier qualification entity
 *
 * @author atlas
 */
@Data
@TableName("supplier_qualification")
public class SupplierQualification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 资质类型: ISO9001/ISO14001/ISO45001 等 / Qualification type */
    private String qualType;

    /** 资质名称 / Qualification name */
    private String qualName;

    /** 证书编号 / Certificate number */
    private String certNo;

    /** 颁发机构 / Issuing authority */
    private String issuingAuthority;

    /** 颁发日期 / Issue date */
    private LocalDate issueDate;

    /** 到期日期 / Expiry date */
    private LocalDate expireDate;

    /** 状态: 1有效 0过期 / Status: 1=valid, 0=expired */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
