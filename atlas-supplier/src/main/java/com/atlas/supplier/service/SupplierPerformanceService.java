package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierPerformance;
import com.atlas.supplier.mapper.SupplierPerformanceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商绩效汇总 Service / Supplier performance summary Service
 *
 * <p>定期计算绩效指标（准时率/合格率/响应时长/投诉次数）+ 自动评级。 /
 * Periodically calculates performance indicators (on-time rate / qualification rate / response time / complaint count) + auto-grading.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierPerformanceService {

    private final SupplierPerformanceMapper performanceMapper;

    /**
     * 计算并保存供应商绩效 / Calculate and save supplier performance
     *
     * <p>预留集成点：准时率、合格率、响应时长需从 delivery / iqc / evaluation 聚合计算。
     * 当前接受外部传入指标。 /
     * Reserved integration point: on-time rate, qualification rate, response time need to be aggregated from delivery / iqc / evaluation.
     * Currently accepts externally passed indicators.</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierPerformance calculate(SupplierPerformance performance) {
        performance.setGrade(determineGrade(performance.getScore()));
        performance.setCreatedAt(LocalDateTime.now());
        performance.setUpdatedAt(LocalDateTime.now());

        // 同一周期覆盖更新 / Overwrite within same period
        SupplierPerformance existing = performanceMapper.selectOne(
                new LambdaQueryWrapper<SupplierPerformance>()
                        .eq(SupplierPerformance::getSupplierId, performance.getSupplierId())
                        .eq(SupplierPerformance::getPeriod, performance.getPeriod()));
        if (existing != null) {
            performance.setId(existing.getId());
            performanceMapper.updateById(performance);
            log.info("供应商绩效更新: supplierId={} period={} grade={}",
                    performance.getSupplierId(), performance.getPeriod(), performance.getGrade());
        } else {
            performanceMapper.insert(performance);
            log.info("供应商绩效创建: supplierId={} period={} grade={}",
                    performance.getSupplierId(), performance.getPeriod(), performance.getGrade());
        }
        return performance;
    }

    /**
     * 按供应商+周期查询绩效 / Query performance by supplier + period
     */
    public SupplierPerformance getBySupplierAndPeriod(Long supplierId, String period) {
        return performanceMapper.selectOne(
                new LambdaQueryWrapper<SupplierPerformance>()
                        .eq(SupplierPerformance::getSupplierId, supplierId)
                        .eq(SupplierPerformance::getPeriod, period));
    }

    /**
     * 按周期查询所有供应商绩效排名 / Query all supplier performance rankings by period
     */
    public List<SupplierPerformance> listByPeriod(String period) {
        return performanceMapper.selectList(
                new LambdaQueryWrapper<SupplierPerformance>()
                        .eq(SupplierPerformance::getPeriod, period)
                        .orderByDesc(SupplierPerformance::getScore));
    }

    /**
     * 分页查询绩效记录 / Paginated query of performance records
     */
    public Page<SupplierPerformance> page(Long supplierId, String period, String grade, int page, int size) {
        LambdaQueryWrapper<SupplierPerformance> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierPerformance::getSupplierId, supplierId);
        }
        if (period != null) {
            wrapper.eq(SupplierPerformance::getPeriod, period);
        }
        if (grade != null) {
            wrapper.eq(SupplierPerformance::getGrade, grade);
        }
        wrapper.orderByDesc(SupplierPerformance::getScore);
        return performanceMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 定时任务：计算上月所有供应商绩效 / Scheduled: calculate monthly performance for all suppliers
     */
    public void calculateMonthly() {
        log.info("[SupplierPerformance] 开始月度绩效统计");
        // Reserved integration point: aggregate from delivery/iqc/evaluation data
        // Current implementation: no-op placeholder
        log.info("[SupplierPerformance] 月度绩效统计完成");
    }

    /**
     * 根据综合得分判定等级 / Determine grade by composite score
     */
    private String determineGrade(BigDecimal score) {
        if (score == null) return "D";
        double s = score.doubleValue();
        if (s >= 90) return "A";
        if (s >= 75) return "B";
        if (s >= 60) return "C";
        return "D";
    }
}
