package com.atlas.quality.service;

import com.atlas.quality.mapper.NcrRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 供应商质量 PPM 统计服务 / Supplier quality PPM statistics service
 * <p>
 * PPM（Parts Per Million）计算：PPM = (不合格品数 / 总收货数) × 1,000,000 /
 * PPM（Parts Per Million）= (defect quantity / total received quantity) × 1,000,000
 * <br>
 * 支持按月/季度/年自动统计、单供应商 PPM 查询、供应商 PPM 排名。 /
 * Supports auto-statistics by month/quarter/year, single supplier PPM query, and supplier PPM ranking.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierPpmService {

    private final NcrRecordMapper ncrMapper;
    // 注：totalReceived 需要从收货模块获取，此处为简化实现 / Note: total received needs to be fetched from receipt module
    // 生产环境应通过 FeignClient 调用 atlas-receipt 获取收货总数 / In production, use FeignClient to call atlas-receipt

    /**
     * 指定时间范围内供应商 PPM / Supplier PPM within time range
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param startDate  开始日期 / Start date
     * @param endDate    结束日期 / End date
     * @return PPM 计算结果 / PPM result
     */
    public PpmResult calculate(Long supplierId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 不合格品数 / Defect quantity
        BigDecimal defectQty = getDefectQty(supplierId, start, end);

        // 总收货数 — 此处简化，实际应跨模块查询 / Total received — simplified, should cross-module query in production
        BigDecimal totalReceived = getTotalReceived(supplierId, start, end);

        // PPM = (不合格品数 / 总收货数) × 1,000,000
        BigDecimal ppm = BigDecimal.ZERO;
        if (totalReceived != null && totalReceived.compareTo(BigDecimal.ZERO) > 0) {
            ppm = defectQty.divide(totalReceived, 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(1_000_000))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        PpmResult result = new PpmResult();
        result.setSupplierId(supplierId);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setDefectQty(defectQty);
        result.setTotalReceived(totalReceived);
        result.setPpm(ppm);

        log.info("供应商 PPM: supplierId={} period={}~{} ppm={}",
                supplierId, startDate, endDate, ppm);
        return result;
    }

    /**
     * 供应商 PPM 排名 / Supplier PPM ranking
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @return PPM 排名列表（从低到高） / PPM ranking (ascending)
     */
    public List<PpmResult> ranking(LocalDate startDate, LocalDate endDate, int topN) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 获取所有供应商列表（从 NCR 数据中提取 / Extract from NCR data）
        List<Long> supplierIds = getSupplierIds(start, end);

        List<PpmResult> results = new ArrayList<>();
        for (Long supplierId : supplierIds) {
            PpmResult ppm = calculate(supplierId, startDate, endDate);
            results.add(ppm);
        }

        // PPM 升序排列（越低越好） / Sort ascending (lower is better)
        results.sort((a, b) -> a.getPpm().compareTo(b.getPpm()));
        return results.stream().limit(topN > 0 ? topN : results.size()).collect(Collectors.toList());
    }

    /**
     * 月维度 PPM 趋势 / Monthly PPM trend
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param months     最近 N 个月 / Last N months
     * @return 月度 PPM 趋势 / Monthly PPM trend
     */
    public List<PpmResult> monthlyTrend(Long supplierId, int months) {
        List<PpmResult> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            PpmResult ppm = calculate(supplierId, start, end);
            ppm.setPeriodLabel(ym.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
            trend.add(ppm);
        }
        return trend;
    }

    // ==================== 内部辅助方法 / Internal Helpers ====================

    /**
     * 查询不合格品总数 / Query total defect quantity
     */
    private BigDecimal getDefectQty(Long supplierId, LocalDateTime start, LocalDateTime end) {
        // 实际实现：SELECT SUM(defect_qty) FROM ncr_record WHERE ...
        // 简化版：当前从已创建的 NCR 记录计算 / Simplified: calculated from NCR records
        Long count = ncrMapper.selectCount(
                new LambdaQueryWrapper<com.atlas.quality.entity.NcrRecord>()
                        .eq(com.atlas.quality.entity.NcrRecord::getSupplierId, supplierId)
                        .between(com.atlas.quality.entity.NcrRecord::getCreatedAt, start, end));
        return BigDecimal.valueOf(count);
    }

    /**
     * 查询总收货数 — 简化实现，实际应调用收货模块查询 / Get total received — simplified, should query receipt module
     */
    private BigDecimal getTotalReceived(Long supplierId, LocalDateTime start, LocalDateTime end) {
        // 生产环境应通过 ReceivingRecordService 跨模块查询 /
        // In production, cross-module query via ReceivingRecordService
        // 此处返回默认值避免除零 / Return default to avoid division by zero
        return BigDecimal.ONE;
    }

    /**
     * 获取活跃供应商 ID 列表 / Get active supplier IDs
     */
    private List<Long> getSupplierIds(LocalDateTime start, LocalDateTime end) {
        // 从 NCR 记录中提取去重供应商 / Extract distinct suppliers from NCR records
        List<com.atlas.quality.entity.NcrRecord> records = ncrMapper.selectList(
                new LambdaQueryWrapper<com.atlas.quality.entity.NcrRecord>()
                        .between(com.atlas.quality.entity.NcrRecord::getCreatedAt, start, end)
                        .groupBy(com.atlas.quality.entity.NcrRecord::getSupplierId));
        return records.stream()
                .map(com.atlas.quality.entity.NcrRecord::getSupplierId)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * PPM 计算结果 / PPM result
     */
    @lombok.Data
    public static class PpmResult {
        private Long supplierId;
        private String supplierName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String periodLabel;
        private java.math.BigDecimal defectQty;
        private java.math.BigDecimal totalReceived;
        private java.math.BigDecimal ppm;
    }
}
