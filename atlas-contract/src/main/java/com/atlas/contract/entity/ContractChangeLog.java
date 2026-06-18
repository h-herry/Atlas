package com.atlas.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同变更日志 / Contract change log
 */
@Data
@TableName("contract_change_log")
public class ContractChangeLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contractId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
