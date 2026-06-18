package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.EvalTemplate;
import com.atlas.supplier.entity.SupplierEvaluation;
import com.atlas.supplier.entity.SupplierEvaluationItem;
import com.atlas.supplier.service.SupplierEvaluationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商绩效评估 Controller — 评估模板 + 绩效考核 + 整改跟踪 /
 * Supplier performance evaluation Controller — evaluation template + performance appraisal + improvement tracking
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/eval")
@RequiredArgsConstructor
public class SupplierEvaluationController {

    private final SupplierEvaluationService evaluationService;

    // ==================== 评估模板 / Evaluation Template ====================

    /** 创建评估模板 / Create evaluation template */
    @PostMapping("/template")
    @RequirePermission("supplier:eval:add")
    public Result<EvalTemplate> createTemplate(@RequestBody EvalTemplate template) {
        return Result.ok(evaluationService.createTemplate(template));
    }

    /** 分页查询评估模板 / Paginated query of templates */
    @GetMapping("/template/page")
    @RequirePermission("supplier:eval:view")
    public Result<Page<EvalTemplate>> pageTemplate(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(evaluationService.pageTemplate(page, size));
    }

    // ==================== 绩效考核 / Performance Appraisal ====================

    /** 生成评估 / Generate evaluation */
    @PostMapping
    @RequirePermission("supplier:eval:add")
    public Result<SupplierEvaluation> createEvaluation(@RequestParam Long supplierId,
                                                        @RequestParam Long templateId,
                                                        @RequestParam String evalPeriod,
                                                        @RequestParam(defaultValue = "1") Integer evalType) {
        return Result.ok(evaluationService.createEvaluation(supplierId, templateId, evalPeriod, evalType));
    }

    /** 打分提交 / Submit scores */
    @PostMapping("/{evaluationId}/scores")
    @RequirePermission("supplier:eval:add")
    public Result<SupplierEvaluation> submitScores(@PathVariable Long evaluationId,
                                                    @RequestBody List<SupplierEvaluationItem> items,
                                                    @RequestParam Long evaluatorId,
                                                    @RequestParam String evaluatorName) {
        return Result.ok(evaluationService.submitScores(evaluationId, items, evaluatorId, evaluatorName));
    }

    /** 供应商确认 / Supplier confirmation */
    @PutMapping("/{evaluationId}/confirm")
    @RequirePermission("supplier:eval:confirm")
    public Result<Void> confirmEvaluation(@PathVariable Long evaluationId) {
        evaluationService.confirmEvaluation(evaluationId);
        return Result.ok();
    }

    /** 发起整改 / Start improvement */
    @PutMapping("/{evaluationId}/improve/start")
    @RequirePermission("supplier:eval:add")
    public Result<Void> startImprovement(@PathVariable Long evaluationId,
                                          @RequestParam String improvementNote,
                                          @RequestParam(required = false) String deadline) {
        evaluationService.startImprovement(evaluationId, improvementNote, deadline);
        return Result.ok();
    }

    /** 整改完成 / Complete improvement */
    @PutMapping("/{evaluationId}/improve/complete")
    @RequirePermission("supplier:eval:add")
    public Result<Void> completeImprovement(@PathVariable Long evaluationId) {
        evaluationService.completeImprovement(evaluationId);
        return Result.ok();
    }

    /** 分页查询评估 / Paginated query of evaluations */
    @GetMapping("/page")
    @RequirePermission("supplier:eval:view")
    public Result<Page<SupplierEvaluation>> pageEvaluation(@RequestParam(required = false) Long supplierId,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(evaluationService.pageEvaluation(supplierId, status, page, size));
    }
}
