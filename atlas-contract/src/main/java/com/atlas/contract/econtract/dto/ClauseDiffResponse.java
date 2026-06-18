package com.atlas.contract.econtract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 条款比对差异响应 / Clause diff response
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClauseDiffResponse {

    /** 比对记录ID / Compare record ID */
    private Long compareId;

    /** 合同ID / Contract ID */
    private Long contractId;

    /** 原始版本 / Source version */
    private String sourceVersion;

    /** 对比目标版本 / Target version */
    private String targetVersion;

    /** 差异结果(JSON) / Diff result (JSON) */
    private String diffResult;

    /** 比对人 / Compared by */
    private String comparedBy;

    /** 比对时间 / Compared at */
    private LocalDateTime comparedAt;
}
