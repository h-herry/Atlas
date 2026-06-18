package com.atlas.purchase.settlement.service;

import com.atlas.purchase.settlement.entity.AgingAnalysis;
import com.atlas.purchase.settlement.mapper.AgingAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 应付账款账龄分析 Service — 按供应商汇总应付余额+账龄分布 /
 * Accounts payable aging analysis Service — aggregates payable balance and aging distribution by supplier
 *
 * <p>账龄分段: 0-30天 / 31-60天 / 61-90天 / 91天以上。超90天自动标记预警 /
 * Aging buckets: 0-30 / 31-60 / 61-90 / 91+ days. Auto-flag alert for 90+ days</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgingAnalysisService {

    private final AgingAnalysisMapper agingAnalysisMapper;

    /**
     * 计算并保存指定日期的账龄分析 / Calculate and save aging analysis for given as-of date
     *
     * @param asOfDate  截止日期 / As-of date
     * @param records   按供应商汇总的账龄数据 [{supplierId, supplierName, totalPayable, aging030, aging3160, aging6190, aging91Plus}]
     * @return 保存的账龄记录列表 / Saved aging records
     */
    public List<AgingAnalysis> calculate(LocalDate asOfDate, List<AgingAnalysis> records) {
        for (AgingAnalysis record : records) {
            record.setAsOfDate(asOfDate);

            // 超90天自动标记预警 / Auto-flag alert for 90+ days
            BigDecimal aging91Plus = record.getAging91Plus();
            if (aging91Plus != null && aging91Plus.compareTo(BigDecimal.ZERO) > 0) {
                record.setOverdueFlag(1);
                log.warn("供应商 {} 存在超90天应付: {}", record.getSupplierName(), aging91Plus);
            } else {
                record.setOverdueFlag(0);
            }

            agingAnalysisMapper.insert(record);
        }
        log.info("账龄分析已完成: asOfDate={}, 供应商数={}", asOfDate, records.size());
        return records;
    }

    /**
     * 查询指定截止日期的账龄记录 GET /api/settlement/aging?asOf=YYYY-MM-DD /
     * Query aging records by as-of date
     */
    public List<AgingAnalysis> queryByDate(LocalDate asOfDate) {
        return agingAnalysisMapper.findByAsOfDate(asOfDate);
    }

    /**
     * 查询超90天预警供应商 / Query suppliers with 90+ day overdue
     */
    public List<AgingAnalysis> findOverdue(LocalDate asOfDate) {
        return agingAnalysisMapper.findOverdue(asOfDate);
    }
}
