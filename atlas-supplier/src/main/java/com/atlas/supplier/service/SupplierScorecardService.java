package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierScorecard;
import com.atlas.supplier.entity.SupplierScorecardItem;
import com.atlas.supplier.mapper.SupplierScorecardItemMapper;
import com.atlas.supplier.mapper.SupplierScorecardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商绩效评分卡 Service — QCD 四维评分 / 自动汇总 / 评分卡查询 /
 * Supplier performance scorecard Service — QCD 4-dimension scoring / auto-summary / scorecard query
 *
 * <p>评分维度权重：
 * 质量 30% | 成本 25% | 交付 25% | 服务 20%，满分 100。 /
 * Scoring dimension weights: Quality 30% | Cost 25% | Delivery 25% | Service 20%, max 100.
 * 评级规则：A(>=90) / B(>=75) / C(>=60) / D(<60)。 /
 * Grade rules: A(>=90) / B(>=75) / C(>=60) / D(<60).</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierScorecardService {

    private final SupplierScorecardMapper scorecardMapper;
    private final SupplierScorecardItemMapper scorecardItemMapper;

    /** QCD 维度权重 / QCD dimension weights */
    private static final BigDecimal WEIGHT_QUALITY = new BigDecimal("30");
    private static final BigDecimal WEIGHT_COST = new BigDecimal("25");
    private static final BigDecimal WEIGHT_DELIVERY = new BigDecimal("25");
    private static final BigDecimal WEIGHT_SERVICE = new BigDecimal("20");

    // ==================== 创建评分卡 / Create Scorecard ====================

    /**
     * 创建评分卡（含明细项） / Create scorecard (with line items)
     *
     * <p>自动汇总各维度得分并计算综合得分和等级。 /
     * Auto-aggregates dimension scores, calculates total score and grade.</p>
     *
     * @param scorecard 评分卡主表 / Scorecard master
     * @param items     明细项列表（按维度分组） / Line items list (grouped by dimension)
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierScorecard create(SupplierScorecard scorecard, List<SupplierScorecardItem> items) {
        // 校验同周期唯一 / Validate period uniqueness
        List<SupplierScorecard> existing = scorecardMapper.selectList(
            new LambdaQueryWrapper<SupplierScorecard>()
                .eq(SupplierScorecard::getSupplierId, scorecard.getSupplierId())
                .eq(SupplierScorecard::getPeriod, scorecard.getPeriod()));
        if (!existing.isEmpty()) {
            throw new BizException(ErrorCode.DATA_EXIST,
                "该供应商在当前周期已存在评分卡: " + scorecard.getPeriod());
        }

        // 汇总各维度得分 / Aggregate dimension scores
        BigDecimal qualitySum = sumByDimension(items, "QUALITY");
        BigDecimal costSum = sumByDimension(items, "COST");
        BigDecimal deliverySum = sumByDimension(items, "DELIVERY");
        BigDecimal serviceSum = sumByDimension(items, "SERVICE");

        // 归一化到权重 / Normalize to weight
        scorecard.setQualityScore(normalize(qualitySum, WEIGHT_QUALITY, items, "QUALITY"));
        scorecard.setCostScore(normalize(costSum, WEIGHT_COST, items, "COST"));
        scorecard.setDeliveryScore(normalize(deliverySum, WEIGHT_DELIVERY, items, "DELIVERY"));
        scorecard.setServiceScore(normalize(serviceSum, WEIGHT_SERVICE, items, "SERVICE"));

        // 综合得分 / Total score
        BigDecimal total = scorecard.getQualityScore()
            .add(scorecard.getCostScore())
            .add(scorecard.getDeliveryScore())
            .add(scorecard.getServiceScore());
        scorecard.setTotalScore(total.setScale(2, RoundingMode.HALF_UP));

        // 评级 / Grade
        scorecard.setGrade(determineGrade(scorecard.getTotalScore()));
        scorecard.setStatus(0); // 草稿 / Draft
        scorecard.setCreatedAt(LocalDateTime.now());
        scorecard.setUpdatedAt(LocalDateTime.now());
        scorecardMapper.insert(scorecard);

        // 写入明细项 / Insert line items
        for (SupplierScorecardItem item : items) {
            item.setScorecardId(scorecard.getId());
            item.setCreatedAt(LocalDateTime.now());
            scorecardItemMapper.insert(item);
        }

        log.info("创建评分卡: supplierId={}, period={}, totalScore={}, grade={}",
            scorecard.getSupplierId(), scorecard.getPeriod(), scorecard.getTotalScore(), scorecard.getGrade());
        return scorecard;
    }

    // ==================== 评分卡查询 / Scorecard Query ====================

    /**
     * 按供应商 + 周期查询评分卡 / Query scorecard by supplier + period
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param period     评分周期（如 2026-Q2），不传则返回全部 / Scoring period, optional
     * @return 评分卡列表 / Scorecard list
     */
    public List<SupplierScorecard> query(Long supplierId, String period) {
        LambdaQueryWrapper<SupplierScorecard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierScorecard::getSupplierId, supplierId)
            .eq(SupplierScorecard::getStatus, 1); // 已发布 / Published
        if (period != null && !period.isBlank()) {
            wrapper.eq(SupplierScorecard::getPeriod, period);
        }
        wrapper.orderByDesc(SupplierScorecard::getPeriod);
        return scorecardMapper.selectList(wrapper);
    }

    /**
     * 查询评分卡明细项 / Query scorecard line items
     *
     * @param scorecardId 评分卡ID / Scorecard ID
     * @return 明细项列表 / Line item list
     */
    public List<SupplierScorecardItem> listItems(Long scorecardId) {
        return scorecardItemMapper.selectList(
            new LambdaQueryWrapper<SupplierScorecardItem>()
                .eq(SupplierScorecardItem::getScorecardId, scorecardId)
                .orderByAsc(SupplierScorecardItem::getDimension));
    }

    /**
     * 按主键查询 / Query by primary key
     */
    public SupplierScorecard getById(Long id) {
        SupplierScorecard scorecard = scorecardMapper.selectById(id);
        if (scorecard == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "评分卡不存在: " + id);
        }
        return scorecard;
    }

    // ==================== 状态操作 / Status Operations ====================

    /**
     * 发布评分卡 / Publish scorecard
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        SupplierScorecard scorecard = getById(id);
        if (scorecard.getStatus() != 0) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅草稿状态可发布");
        }
        scorecard.setStatus(1);
        scorecard.setUpdatedAt(LocalDateTime.now());
        scorecardMapper.updateById(scorecard);
        log.info("发布评分卡: id={}", id);
    }

    // ==================== 内部方法 / Internal ====================

    /**
     * 按维度汇总得分 / Sum scores by dimension
     */
    private BigDecimal sumByDimension(List<SupplierScorecardItem> items, String dimension) {
        return items.stream()
            .filter(i -> dimension.equals(i.getDimension()))
            .map(i -> i.getActualScore() != null ? i.getActualScore() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 归一化得分到权重范围 / Normalize scores to weight range
     *
     * <p>公式：sumScore × weight / totalMaxScore /
     * Formula: sumScore × weight / totalMaxScore</p>
     */
    private BigDecimal normalize(BigDecimal sumScore, BigDecimal weight,
                                  List<SupplierScorecardItem> items, String dimension) {
        BigDecimal totalMax = items.stream()
            .filter(i -> dimension.equals(i.getDimension()))
            .map(i -> i.getMaxScore() != null ? i.getMaxScore() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalMax.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return sumScore.multiply(weight)
            .divide(totalMax, 4, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 根据综合得分确定等级 / Determine grade by total score
     */
    private String determineGrade(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("90")) >= 0) return "A";
        if (totalScore.compareTo(new BigDecimal("75")) >= 0) return "B";
        if (totalScore.compareTo(new BigDecimal("60")) >= 0) return "C";
        return "D";
    }
}
