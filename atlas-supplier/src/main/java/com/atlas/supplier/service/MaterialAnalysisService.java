package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MaterialAnalysis;
import com.atlas.supplier.mapper.MaterialAnalysisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料分析报表 Service / Material analysis report Service
 *
 * <p>库存周转率/呆滞料分析/成本趋势统计。 /
 * Inventory turnover rate / slow-moving material analysis / cost trend statistics.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAnalysisService {

    private final MaterialAnalysisMapper analysisMapper;

    /**
     * 生成/更新物料分析报表 / Create or update material analysis report
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialAnalysis saveOrUpdate(MaterialAnalysis analysis) {
        analysis.setUpdatedAt(LocalDateTime.now());
        // 同一周期覆盖 / Overwrite within same period
        MaterialAnalysis existing = analysisMapper.selectOne(
                new LambdaQueryWrapper<MaterialAnalysis>()
                        .eq(MaterialAnalysis::getMaterialId, analysis.getMaterialId())
                        .eq(MaterialAnalysis::getPeriod, analysis.getPeriod()));
        if (existing != null) {
            analysis.setId(existing.getId());
            analysisMapper.updateById(analysis);
            log.info("物料分析报表更新: materialId={} period={}", analysis.getMaterialId(), analysis.getPeriod());
        } else {
            analysis.setCreatedAt(LocalDateTime.now());
            analysisMapper.insert(analysis);
            log.info("物料分析报表创建: materialId={} period={}", analysis.getMaterialId(), analysis.getPeriod());
        }
        return analysis;
    }

    /**
     * 按物料+周期查询 / Query by material + period
     */
    public MaterialAnalysis getByMaterialAndPeriod(Long materialId, String period) {
        return analysisMapper.selectOne(
                new LambdaQueryWrapper<MaterialAnalysis>()
                        .eq(MaterialAnalysis::getMaterialId, materialId)
                        .eq(MaterialAnalysis::getPeriod, period));
    }

    /**
     * 按周期查询全部物料分析 / Query all material analyses by period
     */
    public List<MaterialAnalysis> listByPeriod(String period) {
        return analysisMapper.selectList(
                new LambdaQueryWrapper<MaterialAnalysis>()
                        .eq(MaterialAnalysis::getPeriod, period)
                        .orderByDesc(MaterialAnalysis::getStockTurnoverRate));
    }

    /**
     * 定时任务：生成季度物料分析报表 / Scheduled: generate quarterly material analysis report
     */
    public void generateQuarterlyReport() {
        log.info("[MaterialAnalysis] 开始生成季度物料分析报表");
        // Reserved integration point: aggregate quarterly turnover/slow-moving/cost data
        // Current implementation: no-op placeholder
        log.info("[MaterialAnalysis] 季度物料分析报表生成完成");
    }

    /**
     * 定时任务：检测呆滞料（超过90天未出库） / Scheduled: detect slow-moving materials (>90 days)
     */
    public void detectSlowMoving() {
        log.info("[MaterialAnalysis] 开始呆滞料检测");
        // Reserved integration point: query materials with last_outbound > 90 days
        // Current implementation: no-op placeholder
        log.info("[MaterialAnalysis] 呆滞料检测完成");
    }

    /**
     * 分页查询分析报表 / Paginated query of analysis reports
     */
    public Page<MaterialAnalysis> page(Long materialId, String period, String costTrend, int page, int size) {
        LambdaQueryWrapper<MaterialAnalysis> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(MaterialAnalysis::getMaterialId, materialId);
        }
        if (period != null) {
            wrapper.eq(MaterialAnalysis::getPeriod, period);
        }
        if (costTrend != null) {
            wrapper.eq(MaterialAnalysis::getPurchaseCostTrend, costTrend);
        }
        wrapper.orderByDesc(MaterialAnalysis::getCreatedAt);
        return analysisMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
