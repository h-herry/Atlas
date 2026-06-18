package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.*;
import com.atlas.supplier.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 供应商绩效评估 Service — 创建模板 → 按模板生成评估 → 多维度打分 → 自动计算总分/定级 → 推送供应商确认 → 整改跟踪 /
 * Supplier performance evaluation Service — create template → generate evaluation by template → multi-dimension scoring → auto-calc total score & grade → push supplier confirmation → improvement tracking
 *
 * <p>评估定级规则: 总分 &ge; 90 → A, &ge; 80 → B, &ge; 60 → C, &lt; 60 → D /
 *    Grading rules: total score &ge; 90 → A, &ge; 80 → B, &ge; 60 → C, &lt; 60 → D
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierEvaluationService {

    private final EvalTemplateMapper evalTemplateMapper;
    private final SupplierEvaluationMapper evaluationMapper;
    private final SupplierEvaluationItemMapper evaluationItemMapper;
    private final SupplierMapper supplierMapper;

    // ==================== 评估模板 / Evaluation Template ====================

    /**
     * 创建评估模板 / Create evaluation template
     */
    @Transactional(rollbackFor = Exception.class)
    public EvalTemplate createTemplate(EvalTemplate template) {
        template.setStatus(1);
        evalTemplateMapper.insert(template);
        log.info("评估模板已创建: templateName={}", template.getTemplateName());
        return template;
    }

    /**
     * 分页查询模板 / Paginated query of templates
     */
    public Page<EvalTemplate> pageTemplate(int page, int size) {
        LambdaQueryWrapper<EvalTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EvalTemplate::getCreatedAt);
        return evalTemplateMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 绩效考核 / Performance Evaluation ====================

    /**
     * 按模板生成评估（草稿状态） / Generate evaluation by template (draft status)
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierEvaluation createEvaluation(Long supplierId, Long templateId, String evalPeriod, Integer evalType) {
        EvalTemplate template = evalTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(ErrorCode.EVAL_TEMPLATE_NOT_EXIST);
        }

        SupplierEvaluation evaluation = new SupplierEvaluation();
        evaluation.setSupplierId(supplierId);
        evaluation.setTemplateId(templateId);
        evaluation.setEvalPeriod(evalPeriod);
        evaluation.setEvalType(evalType);
        evaluation.setStatus(0); // 草稿 / Draft
        evaluationMapper.insert(evaluation);
        log.info("评估已生成: evalId={}, supplierId={}", evaluation.getId(), supplierId);
        return evaluation;
    }

    /**
     * 打分 — 提交各维度明细并自动计算总分/定级 / Scoring — submit dimension details and auto-calc total score & grade
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierEvaluation submitScores(Long evaluationId, List<SupplierEvaluationItem> items,
                                           Long evaluatorId, String evaluatorName) {
        SupplierEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BizException(ErrorCode.EVALUATION_NOT_EXIST);
        }

        // 保存明细项 / Save detail items
        BigDecimal qualityScore = BigDecimal.ZERO;
        BigDecimal deliveryScore = BigDecimal.ZERO;
        BigDecimal costScore = BigDecimal.ZERO;
        BigDecimal serviceScore = BigDecimal.ZERO;
        BigDecimal totalWeighted = BigDecimal.ZERO;

        for (SupplierEvaluationItem item : items) {
            item.setEvaluationId(evaluationId);
            evaluationItemMapper.insert(item);

            BigDecimal weighted = item.getActualScore()
                    .multiply(item.getWeight())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            switch (item.getItemCategory()) {
                case "QUALITY":
                    qualityScore = qualityScore.add(weighted);
                    break;
                case "DELIVERY":
                    deliveryScore = deliveryScore.add(weighted);
                    break;
                case "COST":
                    costScore = costScore.add(weighted);
                    break;
                case "SERVICE":
                    serviceScore = serviceScore.add(weighted);
                    break;
            }
            totalWeighted = totalWeighted.add(weighted);
        }

        evaluation.setQualityScore(qualityScore);
        evaluation.setDeliveryScore(deliveryScore);
        evaluation.setCostScore(costScore);
        evaluation.setServiceScore(serviceScore);
        evaluation.setTotalScore(totalWeighted);
        evaluation.setEvalLevel(calcLevel(totalWeighted));
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatorName(evaluatorName);
        evaluation.setStatus(1); // 已发布 / Published
        evaluationMapper.updateById(evaluation);

        // 同步更新 Supplier 评分 / Sync update Supplier rating
        updateSupplierRating(evaluation.getSupplierId(), totalWeighted, evaluation.getEvalLevel());

        log.info("评估打分完成: evalId={}, totalScore={}, level={}", evaluationId, totalWeighted, evaluation.getEvalLevel());
        return evaluation;
    }

    /**
     * 供应商确认评估结果 / Supplier confirms evaluation result
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmEvaluation(Long evaluationId) {
        SupplierEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BizException(ErrorCode.EVALUATION_NOT_EXIST);
        }
        if (evaluation.getStatus() != 1) {
            throw new BizException(ErrorCode.EVALUATION_ALREADY_CONFIRMED);
        }
        evaluation.setStatus(2); // 供应商确认 / Supplier confirmed
        evaluationMapper.updateById(evaluation);
        log.info("供应商已确认评估: evalId={}", evaluationId);
    }

    /**
     * 整改跟踪 — 发起整改 / 完成整改 / Improvement tracking — start / complete improvement
     */
    @Transactional(rollbackFor = Exception.class)
    public void startImprovement(Long evaluationId, String improvementNote, String deadline) {
        SupplierEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BizException(ErrorCode.EVALUATION_NOT_EXIST);
        }
        evaluation.setStatus(3); // 整改中 / In improvement
        evaluation.setImprovementNote(improvementNote);
        if (deadline != null) {
            evaluation.setImprovementDeadline(java.time.LocalDate.parse(deadline));
        }
        evaluationMapper.updateById(evaluation);
        log.info("整改已发起: evalId={}", evaluationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeImprovement(Long evaluationId) {
        SupplierEvaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            throw new BizException(ErrorCode.EVALUATION_NOT_EXIST);
        }
        evaluation.setStatus(4); // 整改完成 / Improvement completed
        evaluationMapper.updateById(evaluation);
        log.info("整改已完成: evalId={}", evaluationId);
    }

    /**
     * 分页查询评估 / Paginated query of evaluations
     */
    public Page<SupplierEvaluation> pageEvaluation(Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<SupplierEvaluation> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierEvaluation::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(SupplierEvaluation::getStatus, status);
        }
        wrapper.orderByDesc(SupplierEvaluation::getCreatedAt);
        return evaluationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 内部方法 / Internal Methods ====================

    /**
     * 根据总分计算等级: &ge;90→A, &ge;80→B, &ge;60→C, &lt;60→D /
     * Calculate grade by total score: &ge;90→A, &ge;80→B, &ge;60→C, &lt;60→D
     */
    private String calcLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(90)) >= 0) return "A";
        if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) return "B";
        if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) return "C";
        return "D";
    }

    /**
     * 评估完成后更新 Supplier 评级 / Update Supplier rating after evaluation
     * <p>注意: 当前 Supplier 实体无 ratingScore/ratingLevel 字段,
     * 这里通过 status 字段间接表达（已存在的字段映射）。实际生产需扩展 Supplier 实体。 /
     * Note: current Supplier entity lacks ratingScore/ratingLevel fields,
     * using existing grade field as indirection. Production should extend Supplier entity.
     */
    private void updateSupplierRating(Long supplierId, BigDecimal totalScore, String evalLevel) {
        Supplier supplier = supplierMapper.selectById(supplierId);
        if (supplier != null) {
            // 将等级映射到已有 grade 字段: A→1, B→2, C→3, D→4 / Map level to existing grade field
            int grade = switch (evalLevel) {
                case "A" -> 1;
                case "B" -> 2;
                case "C" -> 3;
                default -> 4;
            };
            supplier.setGrade(grade);
            supplierMapper.updateById(supplier);
            log.info("供应商评级已更新: supplierId={}, grade={}, totalScore={}", supplierId, grade, totalScore);
        }
    }
}
