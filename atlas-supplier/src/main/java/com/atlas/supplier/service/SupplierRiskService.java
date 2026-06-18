package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.RiskEvent;
import com.atlas.supplier.entity.SupplierBlacklist;
import com.atlas.supplier.mapper.RiskEventMapper;
import com.atlas.supplier.mapper.SupplierBlacklistMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商风险管理 Service — 风险事件分级处理/闭环跟踪 + 黑名单管理（拉黑/解除/拦截校验） /
 * Supplier risk management Service — risk event tiered handling / closed-loop tracking + blacklist management (add / remove / intercept check)
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierRiskService {

    private final RiskEventMapper riskEventMapper;
    private final SupplierBlacklistMapper blacklistMapper;

    // ==================== 风险事件 / Risk Events ====================

    /**
     * 创建风险事件 / Create risk event
     */
    @Transactional
    public RiskEvent createRiskEvent(RiskEvent event) {
        event.setStatus(0); // 待处理 / Pending
        event.setOccurTime(event.getOccurTime() != null ? event.getOccurTime() : LocalDateTime.now());
        riskEventMapper.insert(event);
        log.info("风险事件已创建: supplierId={}, riskType={}, riskLevel={}",
                event.getSupplierId(), event.getRiskType(), event.getRiskLevel());
        return event;
    }

    /**
     * 处理风险事件 — 开始处理 / Handle risk event — start processing
     */
    @Transactional
    public void startHandle(Long eventId, Long handlerId, String handlerName) {
        RiskEvent event = riskEventMapper.selectById(eventId);
        if (event == null) {
            throw new BizException(ErrorCode.RISK_EVENT_NOT_EXIST);
        }
        event.setStatus(1); // 处理中 / In progress
        event.setHandlerId(handlerId);
        event.setHandlerName(handlerName);
        riskEventMapper.updateById(event);
        log.info("风险事件开始处理: eventId={}, handlerId={}", eventId, handlerId);
    }

    /**
     * 闭环处理 — 标记已解决/已关闭 / Close-loop — mark resolved or closed
     */
    @Transactional
    public void resolveRisk(Long eventId, Integer targetStatus, String handleResult) {
        RiskEvent event = riskEventMapper.selectById(eventId);
        if (event == null) {
            throw new BizException(ErrorCode.RISK_EVENT_NOT_EXIST);
        }
        event.setStatus(targetStatus); // 2已解决 / 3已关闭
        event.setHandleResult(handleResult);
        event.setHandledAt(LocalDateTime.now());
        riskEventMapper.updateById(event);
        log.info("风险事件已闭环: eventId={}, status={}", eventId, targetStatus);
    }

    /**
     * 分页查询风险事件 / Paginated query of risk events
     */
    public Page<RiskEvent> pageRisk(Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<RiskEvent> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(RiskEvent::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(RiskEvent::getStatus, status);
        }
        wrapper.orderByDesc(RiskEvent::getCreatedAt);
        return riskEventMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 黑名单管理 / Blacklist Management ====================

    /**
     * 加入黑名单 / Add to blacklist
     */
    @Transactional
    public SupplierBlacklist addToBlacklist(SupplierBlacklist blacklist) {
        // 幂等: 已存在则更新 / Idempotent: throw if already exists
        SupplierBlacklist existing = blacklistMapper.selectOne(
                new LambdaQueryWrapper<SupplierBlacklist>()
                        .eq(SupplierBlacklist::getSupplierId, blacklist.getSupplierId()));
        if (existing != null) {
            throw new BizException(ErrorCode.BLACKLIST_ALREADY_EXISTS);
        }
        blacklist.setStatus(1); // 生效 / Active
        blacklist.setEffectiveDate(blacklist.getEffectiveDate() != null ? blacklist.getEffectiveDate() : LocalDate.now());
        blacklistMapper.insert(blacklist);
        log.info("供应商已加入黑名单: supplierId={}, blackType={}", blacklist.getSupplierId(), blacklist.getBlackType());
        return blacklist;
    }

    /**
     * 解除黑名单（软解除：status=0） / Remove from blacklist (soft removal: status=0)
     */
    @Transactional
    public void removeFromBlacklist(Long supplierId) {
        SupplierBlacklist blacklist = blacklistMapper.selectOne(
                new LambdaQueryWrapper<SupplierBlacklist>()
                        .eq(SupplierBlacklist::getSupplierId, supplierId)
                        .eq(SupplierBlacklist::getStatus, 1));
        if (blacklist == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        blacklist.setStatus(0); // 解除 / Removed
        blacklistMapper.updateById(blacklist);
        log.info("供应商已移出黑名单: supplierId={}", supplierId);
    }

    /**
     * 黑名单拦截校验 — 供采购/招标/询价等外部模块调用 /
     * Blacklist intercept check — for external modules such as procurement, bidding, RFQ
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return true 表示在黑名单中 / true if blacklisted
     */
    public boolean isBlacklisted(Long supplierId) {
        return blacklistMapper.existsActiveBySupplierId(supplierId);
    }

    /**
     * 校验并抛异常（供外部模块直接调用） /
     * Validate and throw exception (for direct use by external modules)
     *
     * @param supplierId 供应商ID / Supplier ID
     * @throws BizException 如果在黑名单中 / if blacklisted
     */
    public void checkNotBlacklisted(Long supplierId) {
        if (isBlacklisted(supplierId)) {
            throw new BizException(ErrorCode.SUPPLIER_BLACKLISTED);
        }
    }

    /**
     * 分页查询黑名单 / Paginated query of blacklist
     */
    public Page<SupplierBlacklist> pageBlacklist(Integer status, int page, int size) {
        LambdaQueryWrapper<SupplierBlacklist> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SupplierBlacklist::getStatus, status);
        }
        wrapper.orderByDesc(SupplierBlacklist::getCreatedAt);
        return blacklistMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
