package com.atlas.quality.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.quality.entity.PpapElement;
import com.atlas.quality.entity.PpapSubmission;
import com.atlas.quality.service.PpapService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * PPAP提交跟踪 REST API / PPAP Submission Tracking REST API
 * <p>
 * 提供供应商 PPAP 文件提交和采购/质量工程师逐项审核接口。 /
 * Provides supplier PPAP document submission and procurement/quality engineer item review endpoints.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@RestController
@RequestMapping("/api/quality/ppap")
@RequiredArgsConstructor
@Tag(name = "PPAP管理 / PPAP Management")
public class PpapController {

    private final PpapService ppapService;

    /**
     * 创建 PPAP 提交记录 / Create PPAP submission record
     *
     * @param submission PPAP提交实体 / PPAP submission entity
     * @return 创建后的提交记录 / Created submission
     */
    @PostMapping("/submission")
    @RequirePermission("quality:ppap:manage")
    public Result<PpapSubmission> create(@Valid @RequestBody PpapSubmission submission) {
        return Result.ok(ppapService.createSubmission(submission));
    }

    /**
     * 供应商提交 PPAP 要素文件 / Supplier submits PPAP element file
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @param elementCode  要素编码 / Element code
     * @param filePath     文件路径 / File path
     * @return 更新后的要素 / Updated element
     */
    @PostMapping("/submit")
    @RequirePermission("quality:ppap:submit")
    public Result<PpapElement> submitElement(
            @RequestParam @NotNull Long submissionId,
            @RequestParam @NotBlank String elementCode,
            @RequestParam @NotBlank String filePath) {
        return Result.ok(ppapService.submitElement(submissionId, elementCode, filePath));
    }

    /**
     * 审核 PPAP 要素（通过/退回） / Review PPAP element (approve/return)
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @param elementCode  要素编码 / Element code
     * @param approved     是否通过 / Whether approved
     * @param comment      审核意见 / Review comment
     * @param reviewerId   审核人ID / Reviewer ID
     * @return 更新后的要素 / Updated element
     */
    @PostMapping("/review")
    @RequirePermission("quality:ppap:review")
    public Result<PpapElement> reviewElement(
            @RequestParam @NotNull Long submissionId,
            @RequestParam @NotBlank String elementCode,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment,
            @RequestParam @NotNull Long reviewerId) {
        return Result.ok(ppapService.reviewElement(submissionId, elementCode, approved, comment, reviewerId));
    }

    /**
     * 查询 PPAP 提交详情（含要素清单和进度） / Query PPAP submission detail (with element list & progress)
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @return 提交详情 / Submission detail
     */
    @GetMapping("/{submissionId}")
    @RequirePermission("quality:ppap:view")
    public Result<Map<String, Object>> getDetail(@PathVariable Long submissionId) {
        return Result.ok(ppapService.getSubmissionDetail(submissionId));
    }

    /**
     * 按供应商查询 PPAP 提交列表 / Query PPAP submissions by supplier
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param status     状态过滤 / Status filter
     * @param page       当前页 / Current page
     * @param size       每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    @GetMapping("/list")
    @RequirePermission("quality:ppap:view")
    public Result<PageResult<PpapSubmission>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PpapSubmission> pageResult = ppapService.listBySupplier(supplierId, status, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 获取 PPAP 18 要素清单 / Get PPAP 18-element checklist
     *
     * @return 要素定义列表 / Element definition list
     */
    @GetMapping("/elements")
    @RequirePermission("quality:ppap:view")
    public Result<java.util.List<PpapService.ElementDef>> getElements() {
        return Result.ok(PpapService.PPAP_18_ELEMENTS);
    }
}
