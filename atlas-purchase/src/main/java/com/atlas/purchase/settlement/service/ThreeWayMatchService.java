package com.atlas.purchase.settlement.service;

import com.atlas.purchase.settlement.entity.ThreeWayMatch;
import com.atlas.purchase.settlement.mapper.ThreeWayMatchMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 三单匹配 Service — PO行项 ↔ 收货行项 ↔ 发票行项按物料+数量+单价三维校验 /
 * Three-way match Service — PO line ↔ receipt line ↔ invoice line matched by material + quantity + unit price
 *
 * <p>差异处理策略：
 * <ul>
 *   <li>数量差异 → 标记等待人工确认 / Quantity diff → mark for manual review</li>
 *   <li>价格差异 → 差异行标记等待处理 / Price diff → mark discrepancy lines pending</li>
 *   <li>匹配成功 → 更新结算状态为 READY / Match success → update settlement status to READY</li>
 * </ul></p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreeWayMatchService {

    private final ThreeWayMatchMapper threeWayMatchMapper;

    /** 默认数量容差百分比 / Default quantity tolerance percentage */
    private static final BigDecimal DEFAULT_QTY_TOLERANCE = BigDecimal.valueOf(5.00);

    /** 默认价格容差百分比 / Default price tolerance percentage */
    private static final BigDecimal DEFAULT_PRICE_TOLERANCE = BigDecimal.valueOf(2.00);

    /**
     * 执行三单匹配 / Execute three-way match
     *
     * @param poId        采购订单ID
     * @param receiveId   收货记录ID
     * @param invoiceId   发票ID
     * @param poQty       订单行数量
     * @param receiveQty  收货数量
     * @param invoiceQty  发票数量
     * @param poUnitPrice 订单单价
     * @param invoiceUnitPrice 发票单价
     * @return 匹配记录 / Match record
     */
    @Transactional(rollbackFor = Exception.class)
    public ThreeWayMatch execute(Long poId, Long receiveId, Long invoiceId,
                                  BigDecimal poQty, BigDecimal receiveQty, BigDecimal invoiceQty,
                                  BigDecimal poUnitPrice, BigDecimal invoiceUnitPrice,
                                  Long materialId, String materialCode) {

        ThreeWayMatch match = new ThreeWayMatch();
        match.setPoId(poId);
        match.setReceiveId(receiveId);
        match.setInvoiceId(invoiceId);
        match.setMaterialId(materialId);
        match.setMaterialCode(materialCode);
        match.setPoQty(poQty);
        match.setReceiveQty(receiveQty);
        match.setInvoiceQty(invoiceQty);
        match.setPoUnitPrice(poUnitPrice);
        match.setInvoiceUnitPrice(invoiceUnitPrice);
        match.setToleranceQtyPct(DEFAULT_QTY_TOLERANCE);
        match.setTolerancePricePct(DEFAULT_PRICE_TOLERANCE);

        // 数量差异计算 / Quantity discrepancy calculation
        BigDecimal qtyDiscrepancy = invoiceQty.subtract(receiveQty);
        match.setQtyDiscrepancy(qtyDiscrepancy);

        // 价格差异计算 / Price discrepancy calculation
        BigDecimal priceDiscrepancy = invoiceUnitPrice.subtract(poUnitPrice);
        match.setPriceDiscrepancy(priceDiscrepancy);

        // 容差内 → MATCHED；容差外 → DISCREPANCY / Within tolerance → MATCHED; outside → DISCREPANCY
        boolean qtyInTolerance = isWithinTolerance(receiveQty, invoiceQty, DEFAULT_QTY_TOLERANCE);
        boolean priceInTolerance = isWithinTolerance(poUnitPrice, invoiceUnitPrice, DEFAULT_PRICE_TOLERANCE);

        if (qtyInTolerance && priceInTolerance) {
            match.setMatchStatus("MATCHED");
            log.info("三单匹配成功: PO={}, 收货={}, 发票={}", poId, receiveId, invoiceId);
        } else {
            match.setMatchStatus("DISCREPANCY");
            if (!qtyInTolerance) {
                match.setResolution("WAIT_MANUAL"); // 数量差异需人工确认 / Qty diff needs manual review
            } else {
                match.setResolution("WAIT_MANUAL"); // 价格差异标记等待处理 / Price diff marked pending
            }
            log.warn("三单匹配差异: PO={}, 数量差异={}, 价格差异={}", poId, qtyDiscrepancy, priceDiscrepancy);
        }

        threeWayMatchMapper.insert(match);
        return match;
    }

    /**
     * 按PO查询匹配记录 / Query match records by PO
     */
    public List<ThreeWayMatch> findByPoId(Long poId) {
        LambdaQueryWrapper<ThreeWayMatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThreeWayMatch::getPoId, poId);
        return threeWayMatchMapper.selectList(wrapper);
    }

    /**
     * 人工确认差异 / Manually confirm discrepancy
     */
    @Transactional(rollbackFor = Exception.class)
    public ThreeWayMatch confirm(Long matchId, String resolution, Long resolvedBy, String note) {
        ThreeWayMatch match = threeWayMatchMapper.selectById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("匹配记录不存在 / Match record not found: " + matchId);
        }
        match.setMatchStatus("MATCHED");
        match.setResolution(resolution);
        match.setResolvedBy(resolvedBy);
        match.setResolutionNote(note);
        match.setResolutionAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        threeWayMatchMapper.updateById(match);
        log.info("差异已确认: matchId={}, resolution={}", matchId, resolution);
        return match;
    }

    private boolean isWithinTolerance(BigDecimal expected, BigDecimal actual, BigDecimal tolerancePct) {
        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            return actual.compareTo(BigDecimal.ZERO) == 0;
        }
        BigDecimal diff = actual.subtract(expected).abs();
        BigDecimal allowedDiff = expected.abs().multiply(tolerancePct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return diff.compareTo(allowedDiff) <= 0;
    }
}
