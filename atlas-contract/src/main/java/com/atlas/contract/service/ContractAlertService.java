package com.atlas.contract.service;

import com.atlas.contract.entity.Contract;
import com.atlas.contract.entity.ContractAlert;
import com.atlas.contract.mapper.ContractAlertMapper;
import com.atlas.contract.mapper.ContractMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同到期提醒 Service — 每日定时扫描到期前 30/15/7/1 天的合同，推送提醒 /
 * Contract expiry alert Service — daily scan for contracts expiring in 30/15/7/1 days; push alerts
 *
 * <p>到期后自动标记 EXPIRED，未续约前限制发货和收货 /
 * Auto-mark EXPIRED upon expiry; restrict delivery and receiving before renewal</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAlertService {

    private final ContractMapper contractMapper;
    private final ContractAlertMapper alertMapper;

    /** 提醒扫描窗口（天）/ Alert scan windows (days) */
    private static final int[] ALERT_WINDOWS = {30, 15, 7, 1};

    /**
     * 每日定时任务：扫描到期合同 / Daily cron: scan for expiring contracts
     *
     * <p>扫描窗口: 到期前 30/15/7/1 天；到期日当天标记 EXPIRED /
     * Scan windows: 30/15/7/1 days before expiry; mark EXPIRED on the expiry day</p>
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每日凌晨2点执行 / Runs at 2:00 AM daily
    @Transactional(rollbackFor = Exception.class)
    public void scanExpiringContracts() {
        LocalDate today = LocalDate.now();
        List<ContractAlert> alerts = new ArrayList<>();

        // 1. 到期合同自动标记 EXPIRED / Auto-mark expired contracts
        markExpiredContracts(today);

        // 2. 到期前提醒扫描 / Pre-expiry alert scan
        for (int window : ALERT_WINDOWS) {
            LocalDate alertDate = today.plusDays(window);
            List<Contract> expiringContracts = findContractsExpiringOn(alertDate);
            for (Contract contract : expiringContracts) {
                ContractAlert alert = new ContractAlert();
                alert.setContractId(contract.getId());
                alert.setContractNo(contract.getContractNo());
                alert.setAlertType("EXPIRY_" + window);
                alert.setExpireDate(contract.getExpiredAt());
                alert.setAlertDate(today);
                alert.setStatus("PENDING");
                alert.setNotifiedOwner(0);
                alert.setNotifiedSupplier(0);
                alerts.add(alert);
            }
        }

        if (!alerts.isEmpty()) {
            for (ContractAlert alert : alerts) {
                alertMapper.insert(alert);
            }
            log.info("合同到期提醒已生成: {} 条", alerts.size());
            // TODO: 推送提醒至消息中心 (合同负责人 + 供应商) / Push alerts to message center (owner + supplier)
        } else {
            log.debug("无到期合同需要提醒 / No expiring contracts to alert");
        }
    }

    /**
     * 标记已到期合同为 EXPIRED / Mark contracts past expiry as EXPIRED
     */
    private void markExpiredContracts(LocalDate today) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Contract::getExpiredAt, today.minusDays(1))
               .ne(Contract::getStatus, "EXPIRED");
        List<Contract> expiredList = contractMapper.selectList(wrapper);

        for (Contract contract : expiredList) {
            contract.setStatus("EXPIRED");
            contractMapper.updateById(contract);
        }
        if (!expiredList.isEmpty()) {
            log.info("已标记 {} 个合同为 EXPIRED / {0} contracts marked EXPIRED", expiredList.size());
        }
    }

    /**
     * 查询指定日期到期的有效合同 / Find active contracts expiring on a specific date
     */
    private List<Contract> findContractsExpiringOn(LocalDate date) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getExpiredAt, date)
               .eq(Contract::getStatus, "ACTIVE");
        return contractMapper.selectList(wrapper);
    }

    /**
     * 查询待发送的提醒 / Query pending alerts
     */
    public List<ContractAlert> listPending(LocalDate date) {
        return alertMapper.findPendingByDate(date);
    }

    /**
     * 标记提醒已发送 / Mark alert as sent
     */
    @Transactional(rollbackFor = Exception.class)
    public void markSent(Long alertId, boolean notifyOwner, boolean notifySupplier) {
        ContractAlert alert = alertMapper.selectById(alertId);
        if (alert != null) {
            if (notifyOwner) alert.setNotifiedOwner(1);
            if (notifySupplier) alert.setNotifiedSupplier(1);
            alert.setStatus("SENT");
            alertMapper.updateById(alert);
        }
    }

    /**
     * 检查合同是否过期（未续约前限制发货/收货）/ Check if contract expired (restrict delivery/receiving)
     */
    public boolean isExpired(Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        return contract != null && "EXPIRED".equals(contract.getStatus());
    }
}
