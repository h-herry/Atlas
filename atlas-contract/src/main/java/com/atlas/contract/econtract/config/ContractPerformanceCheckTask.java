package com.atlas.contract.econtract.config;

import com.atlas.contract.econtract.service.ContractPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 履约检查定时任务 — 每天 9:00 扫描即将到期/已逾期的履约指标 /
 * Performance check scheduled task — scan due-soon/overdue performance metrics daily at 9:00
 *
 * <p>扫描规则 / Scan rules:
 * <ul>
 *   <li>7 天内到期的履约指标 → 生成 DUE_SOON 告警 / Metrics due within 7 days → generate DUE_SOON alert</li>
 *   <li>已逾期的履约指标 → 标记 BREACHED + 生成 OVERDUE 告警 / Overdue metrics → mark BREACHED + generate OVERDUE alert</li>
 *   <li>已完成(COMPLETED)和已违约(BREACHED)的指标跳过 / Skip COMPLETED and BREACHED metrics</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractPerformanceCheckTask {

    private final ContractPerformanceService performanceService;

    /**
     * 每日 9:00 执行履约预警扫描 /
     * Execute performance alert scan daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkPerformanceAlerts() {
        log.info("===== 履约预警扫描开始 =====");
        try {
            int alertCount = performanceService.scanAndAlert();
            log.info("===== 履约预警扫描完成: 生成 {} 条告警 =====", alertCount);
        } catch (Exception e) {
            log.error("履约预警扫描异常", e);
        }
    }
}
