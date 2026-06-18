package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 条款比对记录实体 — 对应 cnt_clause_compare 表 /
 * Clause compare record entity — corresponds to cnt_clause_compare table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_clause_compare")
public class CntClauseCompare {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 合同ID / Contract ID */
    private Long contractId;

    /** 原始版本号 / Source version */
    private String sourceVersion;

    /** 对比目标版本号 / Target version */
    private String targetVersion;

    /** Diff结果(JSON格式) / Diff result (JSON format) */
    private String diffResult;

    /** 比对人 / Compared by */
    private String comparedBy;

    /** 比对时间 / Compared at */
    private LocalDateTime comparedAt;
}
