package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商实体 — 对应 atlas_supplier 库 supplier 表 / Supplier entity — maps to supplier table in atlas_supplier database
 */
@Data
@TableName("supplier")
public class Supplier {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String supplierNo;
    private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String address;
    private Integer supplierType;
    private String bankAccount;
    private String bankName;
    private Integer qualificationLevel;
    private Integer status;
    private Integer grade;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}