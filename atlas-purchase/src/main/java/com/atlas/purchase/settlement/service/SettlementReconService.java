package com.atlas.purchase.settlement.service;

import com.atlas.purchase.settlement.entity.SettlementRecon;
import com.atlas.purchase.settlement.entity.SettlementReconDetail;
import com.atlas.purchase.settlement.mapper.SettlementReconMapper;
import com.atlas.purchase.settlement.mapper.SettlementReconDetailMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 供应商对账单 Service — 周期内所有交付+结算汇总，支持供应商端确认/异议反馈 /
 * Settlement reconciliation Service — aggregates all deliveries and settlements within a period;
 * supports supplier confirmation and dispute feedback
 *
 * <p>差异明细记录在 settlement_recon_detail 表 / Discrepancy details in settlement_recon_detail table</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementReconService {

    private final SettlementReconMapper reconMapper;
    private final SettlementReconDetailMapper detailMapper;

    /**
     * 生成对账单 / Generate reconciliation statement
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementRecon generate(Long supplierId, String supplierName,
                                     LocalDate periodStart, LocalDate periodEnd,
                                     BigDecimal reconAmount, Long createdBy) {

        SettlementRecon recon = new SettlementRecon();
        recon.setReconNo("RECON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        recon.setSupplierId(supplierId);
        recon.setSupplierName(supplierName);
        recon.setPeriodStart(periodStart);
        recon.setPeriodEnd(periodEnd);
        recon.setReconAmount(reconAmount);
        recon.setDiffAmount(BigDecimal.ZERO);
        recon.setStatus("DRAFT");
        recon.setCreatedBy(createdBy);
        reconMapper.insert(recon);

        log.info("对账单已生成: reconNo={}, supplierId={}, amount={}", recon.getReconNo(), supplierId, reconAmount);
        return recon;
    }

    /**
     * 发送对账单给供应商 / Send reconciliation to supplier
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementRecon send(Long reconId) {
        SettlementRecon recon = reconMapper.selectById(reconId);
        if (recon == null) {
            throw new IllegalArgumentException("对账单不存在 / Recon not found: " + reconId);
        }
        recon.setStatus("SUPPLIER_PENDING");
        recon.setSentAt(LocalDateTime.now());
        recon.setUpdatedAt(LocalDateTime.now());
        reconMapper.updateById(recon);
        log.info("对账单已发送: reconId={}, supplierId={}", reconId, recon.getSupplierId());
        return recon;
    }

    /**
     * 供应商确认对账 / Supplier confirms reconciliation
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementRecon confirm(Long reconId, BigDecimal confirmedAmount) {
        SettlementRecon recon = reconMapper.selectById(reconId);
        if (recon == null) {
            throw new IllegalArgumentException("对账单不存在 / Recon not found: " + reconId);
        }
        recon.setConfirmedAmount(confirmedAmount);
        recon.setDiffAmount(recon.getReconAmount().subtract(confirmedAmount));
        recon.setStatus("CONFIRMED");
        recon.setSupplierConfirmedAt(LocalDateTime.now());
        recon.setUpdatedAt(LocalDateTime.now());
        reconMapper.updateById(recon);
        log.info("供应商已确认: reconId={}, confirmedAmount={}", reconId, confirmedAmount);
        return recon;
    }

    /**
     * 供应商提交异议 / Supplier submits dispute
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitDispute(Long reconId, List<SettlementReconDetail> details) {
        SettlementRecon recon = reconMapper.selectById(reconId);
        if (recon == null) {
            throw new IllegalArgumentException("对账单不存在 / Recon not found: " + reconId);
        }
        recon.setStatus("DISPUTED");
        recon.setUpdatedAt(LocalDateTime.now());
        reconMapper.updateById(recon);

        for (SettlementReconDetail detail : details) {
            detail.setReconId(reconId);
            detail.setStatus("OPEN");
            detailMapper.insert(detail);
        }
        log.info("供应商已提交异议: reconId={}, 差异项数={}", reconId, details.size());
    }

    /**
     * 按供应商查询对账单列表 / Query reconciliations by supplier
     */
    public List<SettlementRecon> listBySupplier(Long supplierId) {
        LambdaQueryWrapper<SettlementRecon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettlementRecon::getSupplierId, supplierId)
               .orderByDesc(SettlementRecon::getCreatedAt);
        return reconMapper.selectList(wrapper);
    }

    /**
     * 查询对账单差异明细 / Query reconciliation discrepancy details
     */
    public List<SettlementReconDetail> listDetails(Long reconId) {
        LambdaQueryWrapper<SettlementReconDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettlementReconDetail::getReconId, reconId);
        return detailMapper.selectList(wrapper);
    }

    /**
     * 解决差异 / Resolve discrepancy
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveDiscrepancy(Long detailId, Long resolvedBy, String note) {
        SettlementReconDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new IllegalArgumentException("差异明细不存在 / Detail not found: " + detailId);
        }
        detail.setStatus("RESOLVED");
        detail.setResolvedBy(resolvedBy);
        detail.setResolvedAt(LocalDateTime.now());
        detail.setNote(note);
        detailMapper.updateById(detail);
        log.info("差异已解决: detailId={}", detailId);
    }
}
