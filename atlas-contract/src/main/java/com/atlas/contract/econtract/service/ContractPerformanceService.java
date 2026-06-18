package com.atlas.contract.econtract.service;

import com.atlas.contract.econtract.dto.PerformanceCreateRequest;
import com.atlas.contract.econtract.enums.PerformanceStatus;
import com.atlas.contract.econtract.mapper.CntPerformanceMapper;
import com.atlas.contract.econtract.mapper.CntPerformanceAlertMapper;
import com.atlas.contract.econtract.model.CntPerformance;
import com.atlas.contract.econtract.model.CntPerformanceAlert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 履约跟踪服务 — CRUD + 进度更新 + 违约告警 /
 * Contract performance tracking service — CRUD + progress update + breach alert
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractPerformanceService extends ServiceImpl<CntPerformanceMapper, CntPerformance> {

    private final CntPerformanceMapper performanceMapper;
    private final CntPerformanceAlertMapper alertMapper;

    // ============ 创建履约指标 / Create Performance Metric ============

    /**
     * 创建履约指标 / Create performance metric
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(PerformanceCreateRequest request) {
        CntPerformance performance = new CntPerformance();
        performance.setContractId(request.getContractId());
        performance.setClauseRef(request.getClauseRef());
        performance.setMetricName(request.getMetricName());
        performance.setMetricType(request.getMetricType());
        performance.setTargetValue(request.getTargetValue());
        performance.setActualValue(request.getActualValue());
        performance.setStatus(PerformanceStatus.NOT_STARTED.getCode());
        performance.setDueDate(request.getDueDate());
        performance.setRemark(request.getRemark());
        save(performance);
        log.info("履约指标已创建: id={} metric={} contractId={}",
                performance.getId(), request.getMetricName(), request.getContractId());
        return performance.getId();
    }

    // ============ 查询履约列表 / Query Performance List ============

    /**
     * 查询合同的所有履约指标 / Query all performance metrics by contract ID
     */
    public List<CntPerformance> listByContractId(Long contractId) {
        LambdaQueryWrapper<CntPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntPerformance::getContractId, contractId)
               .orderByAsc(CntPerformance::getDueDate);
        return performanceMapper.selectList(wrapper);
    }

    // ============ 更新履约进度 / Update Performance Progress ============

    /**
     * 更新履约进度 / Update performance progress
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProgress(Long id, String status, String actualValue, String remark) {
        CntPerformance performance = getById(id);
        if (performance == null) {
            throw new IllegalArgumentException("履约指标不存在: " + id);
        }

        performance.setStatus(status);
        if (actualValue != null) {
            performance.setActualValue(actualValue);
        }
        if (remark != null) {
            performance.setRemark(remark);
        }
        if (PerformanceStatus.COMPLETED.getCode().equals(status)
                || PerformanceStatus.BREACHED.getCode().equals(status)) {
            performance.setCompletedAt(LocalDateTime.now());
        }
        return updateById(performance);
    }

    // ============ 违约预警 / Breach Alert ============

    /**
     * 查询合同的违约预警 / Query breach alerts by contract ID
     */
    public List<CntPerformanceAlert> listAlertsByContractId(Long contractId) {
        // 先查该合同的所有履约指标 / Find all performance metrics for the contract
        List<CntPerformance> performances = listByContractId(contractId);
        if (performances.isEmpty()) {
            return List.of();
        }

        List<Long> performanceIds = performances.stream()
                .map(CntPerformance::getId)
                .toList();

        LambdaQueryWrapper<CntPerformanceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CntPerformanceAlert::getPerformanceId, performanceIds)
               .orderByDesc(CntPerformanceAlert::getCreatedAt);
        return alertMapper.selectList(wrapper);
    }

    // ============ 定时任务用: 扫描即将到期/已逾期履约指标 / Scheduled Scan ============

    /**
     * 扫描即将到期(7天内)和已逾期的履约指标，生成告警 /
     * Scan performance metrics due within 7 days or overdue, generate alerts
     *
     * @return 生成的告警数量 / Number of alerts generated
     */
    @Transactional(rollbackFor = Exception.class)
    public int scanAndAlert() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        // 查找7天内到期或已逾期的履约指标（排除已完成和已违约的） /
        // Find metrics due within 7 days or overdue (exclude COMPLETED and BREACHED)
        LambdaQueryWrapper<CntPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(CntPerformance::getDueDate, sevenDaysLater)
               .notIn(CntPerformance::getStatus,
                       PerformanceStatus.COMPLETED.getCode(),
                       PerformanceStatus.BREACHED.getCode());
        List<CntPerformance> dueList = performanceMapper.selectList(wrapper);

        int alertCount = 0;
        for (CntPerformance performance : dueList) {
            String alertType;
            String alertMessage;

            if (performance.getDueDate().isBefore(today)) {
                // 已逾期 / Overdue
                alertType = "OVERDUE";
                alertMessage = String.format(
                    "履约指标 [%s] (合同ID: %d) 已逾期，到期日期: %s，当前状态: %s",
                    performance.getMetricName(), performance.getContractId(),
                    performance.getDueDate(), performance.getStatus()
                );
                // 自动标记为 BREACHED / Auto-mark as BREACHED
                performance.setStatus(PerformanceStatus.BREACHED.getCode());
                performanceMapper.updateById(performance);
            } else {
                // 即将到期 / Due soon
                alertType = "DUE_SOON";
                long daysLeft = performance.getDueDate().toEpochDay() - today.toEpochDay();
                alertMessage = String.format(
                    "履约指标 [%s] (合同ID: %d) 即将到期，还有 %d 天，到期日期: %s",
                    performance.getMetricName(), performance.getContractId(),
                    daysLeft, performance.getDueDate()
                );
            }

            CntPerformanceAlert alert = new CntPerformanceAlert();
            alert.setPerformanceId(performance.getId());
            alert.setAlertType(alertType);
            alert.setAlertMessage(alertMessage);
            alert.setNotifyTo("合同管理员");
            alert.setIsSent(0);
            alertMapper.insert(alert);
            alertCount++;
        }

        log.info("履约预警扫描完成: 扫描到 {} 个指标，生成 {} 条告警", dueList.size(), alertCount);
        return alertCount;
    }
}
