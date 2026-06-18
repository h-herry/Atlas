package com.atlas.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PPAP要素明细实体 — 对应 ppap_element /
 * PPAP element detail entity — maps to ppap_element
 * <p>
 * 每个 PPAP 提交包含最多 18 个要素（按 PPAP 等级不同要求数不同），
 * 每个要素可独立上传文件、独立审核。 /
 * Each PPAP submission contains up to 18 elements (varies by PPAP level);
 * each element can independently upload files and be reviewed.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("ppap_element")
public class PpapElement {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联PPAP提交ID / Related PPAP submission ID */
    private Long submissionId;

    /** 要素编码(如 DFMEA/PFMEA/CONTROL_PLAN) / Element code (e.g. DFMEA/PFMEA/CONTROL_PLAN) */
    private String elementCode;

    /** 要素名称 / Element name */
    private String elementName;

    /** 要素序号(1~18) / Element sequence (1~18) */
    private Integer elementSeq;

    /** 当前PPAP等级是否要求本要素 / Whether required for current PPAP level */
    private Integer isRequired;

    /** 是否已提交 / Whether submitted */
    private Integer submitted;

    /** 是否已批准 / Whether approved */
    private Integer approved;

    /** 审核意见 / Review comment */
    private String comment;

    /** 提交文件路径 / Submission file path */
    private String filePath;

    /** 审核人 / Reviewed by */
    private Long reviewedBy;

    /** 审核时间 / Review time */
    private LocalDateTime reviewedAt;
}
