package com.atlas.supplier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 供应商模块定时任务 Service / Supplier module scheduled task Service
 *
 * <p>每月自动生成绩效汇总报表 + 每季度生成物料分析报表 + 每日呆滞料检测。
 * <br>所有定时任务通过 {@code @Async("taskExecutor")} 异步执行并记录耗时。 /
 * Monthly performance summary report + quarterly material analysis report + daily slow-moving detection.
 * <br>All scheduled tasks run asynchronously via {@code @Async("taskExecutor")} with elapsed time logging.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final SupplierPerformanceService performanceService;
    private final MaterialAnalysisService analysisService;

    /**
     * 每月1日凌晨2点自动统计上月供应商绩效 /
     * Auto-calculate last month supplier performance at 2:00 AM on the 1st
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Async("taskExecutor")
    public void monthlyPerformanceReport() {
        long start = System.currentTimeMillis();
        try {
            log.info("[定时任务] 月度供应商绩效统计开始");
            performanceService.calculateMonthly();
            log.info("[定时任务] 月度供应商绩效统计完成, 耗时: {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("[定时任务] 月度供应商绩效统计失败, 耗时: {}ms", System.currentTimeMillis() - start, e);
        }
    }

    /**
     * 每季度首月1日凌晨3点自动生成物料分析报表 /
     * Auto-generate quarterly material analysis report at 3:00 AM on the 1st of Jan/Apr/Jul/Oct
     */
    @Scheduled(cron = "0 0 3 1 1,4,7,10 ?")
    @Async("taskExecutor")
    public void quarterlyMaterialAnalysis() {
        long start = System.currentTimeMillis();
        try {
            log.info("[定时任务] 季度物料分析报表生成开始");
            analysisService.generateQuarterlyReport();
            log.info("[定时任务] 季度物料分析报表生成完成, 耗时: {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("[定时任务] 季度物料分析报表生成失败, 耗时: {}ms", System.currentTimeMillis() - start, e);
        }
    }

    /**
     * 每日凌晨4点检测呆滞料（超过90天未出库的物料） /
     * Daily slow-moving material detection (materials not shipped for over 90 days) at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Async("taskExecutor")
    public void slowMovingCheck() {
        long start = System.currentTimeMillis();
        try {
            log.info("[定时任务] 呆滞料检测开始");
            analysisService.detectSlowMoving();
            log.info("[定时任务] 呆滞料检测完成, 耗时: {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("[定时任务] 呆滞料检测失败, 耗时: {}ms", System.currentTimeMillis() - start, e);
        }
    }
}
