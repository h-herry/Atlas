package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.entity.SingleSourcePurchase;
import com.atlas.purchase.mapper.SingleSourcePurchaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单一来源采购 Service / Single source procurement service
 *
 * <p>状态流转：DRAFT(0) → NEGOTIATING(1) → COMPLETED(2) / Status flow: DRAFT(0) → NEGOTIATING(1) → COMPLETED(2)
 * <br>任意非终态可跳转到 TERMINATED(3)。 / Any non-terminal state can jump to TERMINATED(3).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SingleSourceService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_NEGOTIATING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_TERMINATED = 3;

    private final SingleSourcePurchaseMapper singleSourcePurchaseMapper;

    // ==================== 生命周期 / Lifecycle ====================

    @Transactional(rollbackFor = Exception.class)
    public SingleSourcePurchase createFromOrder(PurchaseOrder order) {
        SingleSourcePurchase purchase = new SingleSourcePurchase();
        purchase.setPurchaseOrderId(order.getId());
        purchase.setSupplierId(order.getSupplierId());
        purchase.setStatus(STATUS_DRAFT);
        singleSourcePurchaseMapper.insert(purchase);
        log.info("创建单一来源采购: id={}, orderId={}", purchase.getId(), order.getId());
        return purchase;
    }

    /**
     * 设置供应商和理由 — 进入谈判中 / Set supplier and reason — enter negotiation phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void startNegotiation(Long id, String supplierName, String singleSourceReason,
                                  BigDecimal negotiationAmount) {
        SingleSourcePurchase purchase = getById(id);
        if (purchase.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅草稿状态可开始谈判 / Only DRAFT status can start negotiation");
        }
        if (singleSourceReason == null || singleSourceReason.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "单一来源理由不能为空 / Single source reason is required");
        }
        purchase.setSupplierName(supplierName);
        purchase.setSingleSourceReason(singleSourceReason);
        purchase.setNegotiationAmount(negotiationAmount);
        purchase.setStatus(STATUS_NEGOTIATING);
        singleSourcePurchaseMapper.updateById(purchase);
        log.info("开始单一来源谈判: id={}, supplier={}", id, supplierName);
    }

    /**
     * 谈判完成 — 成交 / Negotiation complete — transact
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, BigDecimal finalAmount, String negotiatedBy) {
        SingleSourcePurchase purchase = getById(id);
        if (purchase.getStatus() != STATUS_NEGOTIATING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅谈判中状态可成交 / Only NEGOTIATING status can complete");
        }
        purchase.setFinalAmount(finalAmount);
        purchase.setNegotiatedBy(negotiatedBy);
        purchase.setNegotiatedAt(LocalDateTime.now());
        purchase.setStatus(STATUS_COMPLETED);
        singleSourcePurchaseMapper.updateById(purchase);
        log.info("单一来源成交: id={}, 最终金额={}", id, finalAmount);
    }

    /**
     * 终止采购 / Terminate procurement
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long id) {
        SingleSourcePurchase purchase = getById(id);
        if (purchase.getStatus() == STATUS_COMPLETED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已成交不可终止 / Completed cannot be terminated");
        }
        purchase.setStatus(STATUS_TERMINATED);
        singleSourcePurchaseMapper.updateById(purchase);
        log.info("终止单一来源采购: id={}", id);
    }

    // ==================== 查询 / Query ====================

    /**
     * 查询单一来源采购详情 / Query single source procurement detail
     */
    public SingleSourcePurchase getById(Long id) {
        SingleSourcePurchase purchase = singleSourcePurchaseMapper.selectById(id);
        if (purchase == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "单一来源采购记录不存在: " + id);
        }
        return purchase;
    }

    /**
     * 分页查询单一来源采购 / Paginated query of single source procurements
     */
    public IPage<SingleSourcePurchase> page(IPage<SingleSourcePurchase> page, String keyword, Integer status) {
        LambdaQueryWrapper<SingleSourcePurchase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SingleSourcePurchase::getSupplierName, keyword);
        }
        if (status != null) {
            wrapper.eq(SingleSourcePurchase::getStatus, status);
        }
        wrapper.orderByDesc(SingleSourcePurchase::getCreatedAt);
        return singleSourcePurchaseMapper.selectPage(page, wrapper);
    }
}
