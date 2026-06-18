package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.AlertRecord;
import com.atlas.supplier.entity.AlertRule;
import com.atlas.supplier.mapper.AlertRecordMapper;
import com.atlas.supplier.mapper.AlertRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 供应商预警管理 Service — 配置预警规则 → 定时扫描资质到期/交期延迟/质量缺陷 → 自动生成预警记录 → 通知责任人 /
 * Supplier alert management Service — configure alert rules → scheduled scan for cert expiry / delivery delay / quality defects → auto-generate alert records → notify responsible parties
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierAlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertRecordMapper alertRecordMapper;

    // ==================== 预警规则 / Alert Rules ====================

    /**
     * 创建预警规则 / Create alert rule
     */
    @Transactional
    public AlertRule createRule(AlertRule rule) {
        rule.setIsEnabled(1);
        alertRuleMapper.insert(rule);
        log.info("预警规则已创建: ruleName={}, ruleType={}", rule.getRuleName(), rule.getRuleType());
        return rule;
    }

    /**
     * 更新预警规则 / Update alert rule
     */
    @Transactional
    public AlertRule updateRule(AlertRule rule) {
        AlertRule existing = alertRuleMapper.selectById(rule.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.ALERT_RULE_NOT_EXIST);
        }
        alertRuleMapper.updateById(rule);
        return rule;
    }

    /**
     * 启用/停用预警规则 / Enable or disable alert rule
     */
    @Transactional
    public void toggleRule(Long ruleId, boolean enabled) {
        AlertRule rule = alertRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BizException(ErrorCode.ALERT_RULE_NOT_EXIST);
        }
        rule.setIsEnabled(enabled ? 1 : 0);
        alertRuleMapper.updateById(rule);
        log.info("预警规则状态已变更: ruleId={}, enabled={}", ruleId, enabled);
    }

    /**
     * 分页查询预警规则 / Paginated query of alert rules
     */
    public Page<AlertRule> pageRule(Integer isEnabled, int page, int size) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (isEnabled != null) {
            wrapper.eq(AlertRule::getIsEnabled, isEnabled);
        }
        wrapper.orderByDesc(AlertRule::getCreatedAt);
        return alertRuleMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 预警记录 / Alert Records ====================

    /**
     * 自动生成预警记录（由定时任务调用） / Auto-generate alert record (invoked by scheduled task)
     *
     * @param supplierId  供应商ID / Supplier ID
     * @param ruleId      关联规则ID / Associated rule ID
     * @param alertType   预警类型 / Alert type
     * @param alertLevel  预警级别 / Alert level
     * @param alertTitle  预警标题 / Alert title
     * @param alertContent 预警详情 / Alert detail
     */
    @Transactional
    public AlertRecord generateAlert(Long supplierId, Long ruleId, String alertType,
                                     String alertLevel, String alertTitle, String alertContent) {
        AlertRecord record = new AlertRecord();
        record.setRuleId(ruleId);
        record.setSupplierId(supplierId);
        record.setAlertType(alertType);
        record.setAlertLevel(alertLevel);
        record.setAlertTitle(alertTitle);
        record.setAlertContent(alertContent);
        record.setIsRead(0);
        record.setIsHandled(0);
        alertRecordMapper.insert(record);
        log.info("预警已生成: supplierId={}, alertType={}, alertLevel={}", supplierId, alertType, alertLevel);
        // TODO: 集成通知服务，根据 notifyUsers 发送站内信/邮件
        return record;
    }

    /**
     * 标记预警已读 / Mark alert as read
     */
    @Transactional
    public void markRead(Long recordId) {
        AlertRecord record = alertRecordMapper.selectById(recordId);
        if (record != null) {
            record.setIsRead(1);
            alertRecordMapper.updateById(record);
        }
    }

    /**
     * 处理预警 — 标记已处理 / Handle alert — mark as handled
     */
    @Transactional
    public void handleAlert(Long recordId, Long handlerId) {
        AlertRecord record = alertRecordMapper.selectById(recordId);
        if (record != null) {
            record.setIsHandled(1);
            record.setHandlerId(handlerId);
            record.setHandleTime(LocalDateTime.now());
            alertRecordMapper.updateById(record);
            log.info("预警已处理: recordId={}, handlerId={}", recordId, handlerId);
        }
    }

    /**
     * 分页查询预警记录 / Paginated query of alert records
     */
    public Page<AlertRecord> pageAlert(Long supplierId, Integer isRead, int page, int size) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(AlertRecord::getSupplierId, supplierId);
        }
        if (isRead != null) {
            wrapper.eq(AlertRecord::getIsRead, isRead);
        }
        wrapper.orderByDesc(AlertRecord::getCreatedAt);
        return alertRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 统计供应商未读预警数 / Count unread alerts for a supplier
     */
    public long countUnreadBySupplier(Long supplierId) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getSupplierId, supplierId)
               .eq(AlertRecord::getIsRead, 0);
        return alertRecordMapper.selectCount(wrapper);
    }
}
