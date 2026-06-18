package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SettlementBill;
import com.atlas.supplier.entity.SettlementThreeWayMatch;
import com.atlas.supplier.feign.MessageFeignClient;
import com.atlas.supplier.mapper.SettlementBillMapper;
import com.atlas.supplier.mapper.SettlementThreeWayMatchMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 结算管理 Service — 创建结算单 → 供应商确认 → 采购方确认 → 财务结算 → 三单匹配（订单/入库单/发票） /
 * Settlement management Service — create bill → supplier confirm → purchaser confirm → financial settle → three-way match (order / receipt / invoice)
 *
 * <p>状态机: / State machine:
 * <pre>
 *   待供应商确认(1) → 双方已确认(2) → 已结算(3)
 *   Pending supplier(1) → Both confirmed(2) → Settled(3)
 * </pre>
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementBillMapper settlementBillMapper;
    private final SettlementThreeWayMatchMapper matchMapper;
    private final MessageFeignClient messageFeignClient;

    // ==================== 结算单 / Settlement Bill ====================

    /**
     * 新增结算单 / Create settlement bill
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementBill save(SettlementBill bill) {
        if (bill.getInvoiceAmount() == null) {
            bill.setInvoiceAmount(BigDecimal.ZERO);
        }
        bill.setStatus(bill.getStatus() != null ? bill.getStatus() : 1);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        settlementBillMapper.insert(bill);
        log.info("结算单已创建: id={}, amount={}, supplierId={}", bill.getId(), bill.getInvoiceAmount(), bill.getSupplierId());
        return bill;
    }

    /**
     * 按ID查询结算单 / Query settlement bill by ID
     */
    public SettlementBill getById(Long id) {
        SettlementBill bill = settlementBillMapper.selectById(id);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        return bill;
    }

    /**
     * 更新结算单 / Update settlement bill
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SettlementBill bill) {
        bill.setUpdatedAt(LocalDateTime.now());
        int rows = settlementBillMapper.updateById(bill);
        log.info("结算单更新: id={}, rows={}", bill.getId(), rows);
        return rows > 0;
    }

    /**
     * 分页查询结算单 / Paginated query of settlement bills
     */
    public Page<SettlementBill> page(Long supplierId, Integer status, String invoiceNo, int page, int size) {
        LambdaQueryWrapper<SettlementBill> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SettlementBill::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(SettlementBill::getStatus, status);
        }
        if (invoiceNo != null && !invoiceNo.isBlank()) {
            wrapper.like(SettlementBill::getInvoiceNo, invoiceNo);
        }
        wrapper.orderByDesc(SettlementBill::getUpdatedAt);
        return settlementBillMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 分页查询结算单（简化版） / Paginated query (simplified)
     */
    public Page<SettlementBill> page(Long supplierId, Integer status, int page, int size) {
        return page(supplierId, status, null, page, size);
    }

    /**
     * 生成结算单（从对账单） / Generate settlement bill from reconciliation
     */
    public SettlementBill generate(Long reconciliationId) {
        // Reserved: reconcile against purchase order and receipt data
        SettlementBill bill = new SettlementBill();
        bill.setSupplierId(reconciliationId); // placeholder mapping
        bill.setBillAmount(BigDecimal.ZERO);
        bill.setStatus(0);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        settlementBillMapper.insert(bill);
        log.info("结算单已生成: id={}, reconciliationId={}", bill.getId(), reconciliationId);

        // 实时消息推送: 对账单生成 / Real-time message push: settlement statement generated
        pushSettlementEvent(bill, "supplier_settlement.statement_generated",
                "对账单生成 / Statement Generated",
                "结算单 " + bill.getId() + " 已生成，金额 ￥" + bill.getBillAmount());

        return bill;
    }

    /**
     * 三单匹配（简化版） / Three-way match (simplified)
     */
    public SettlementThreeWayMatch threeWayMatch(Long settlementId) {
        // Reserved: auto-match against linked purchase order and receipt
        return threeWayMatch(settlementId, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * 发起结算（带审批人） / Initiate settlement (with approver)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean settle(Long settlementId, String approver) {
        log.info("结算发起: settlementId={}, approver={}", settlementId, approver);
        return settle(settlementId);
    }

    /**
     * 供应商确认 / Supplier confirms — status 1→2
     */
    public boolean supplierConfirm(Long id) {
        SettlementBill bill = getById(id);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        if (bill.getStatus() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "仅待确认状态可由供应商确认");
        }
        bill.setStatus(2);
        return updateById(bill);
    }

    /**
     * 采购方确认 / Purchaser confirms — status 1→2
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean purchaserConfirm(Long id) {
        SettlementBill bill = getById(id);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        if (bill.getStatus() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "仅供应商已确认状态可由采购方确认");
        }
        bill.setStatus(2);
        return updateById(bill);
    }

    /**
     * 完成结算 — 状态 2→3 / Complete settlement — status 2→3
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean settle(Long id) {
        SettlementBill bill = getById(id);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        if (bill.getStatus() != 2) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "仅双方确认状态可结算");
        }
        bill.setStatus(3);
        boolean result = updateById(bill);

        // 实时消息推送: 付款状态变更 / Real-time message push: payment status updated
        pushSettlementEvent(bill, "supplier_settlement.payment_updated",
                "付款状态变更 / Payment Status Updated",
                "结算单 " + bill.getId() + " 已结算完成，状态更新为【已结算】。");

        return result;
    }

    /**
     * 三单匹配 — 比较订单金额、入库金额、发票金额 / Three-way match — compare order amount, receipt amount, invoice amount
     *
     * @param settlementId    结算单ID / Settlement bill ID
     * @param purchaseOrderId 采购订单ID / Purchase order ID
     * @param receiptId       入库单ID / Receipt ID
     * @param invoiceNo       发票号 / Invoice number
     * @param orderAmount     订单金额 / Order amount
     * @param receiptAmount   入库金额 / Receipt amount
     * @param invoiceAmount   发票金额 / Invoice amount
     * @return 匹配记录 / Match record
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementThreeWayMatch threeWayMatch(Long settlementId, Long purchaseOrderId,
                                                  Long receiptId, String invoiceNo,
                                                  BigDecimal orderAmount, BigDecimal receiptAmount,
                                                  BigDecimal invoiceAmount) {
        SettlementThreeWayMatch match = new SettlementThreeWayMatch();
        match.setSettlementId(settlementId);
        match.setPurchaseOrderId(purchaseOrderId);
        match.setReceiptId(receiptId);
        match.setInvoiceNo(invoiceNo);
        match.setOrderAmount(orderAmount);
        match.setReceiptAmount(receiptAmount);
        match.setInvoiceAmount(invoiceAmount);

        // 三单金额对比（允许 ±0.01 浮动误差） / Three-way amount comparison (allow ±0.01 tolerance)
        boolean orderMatch = orderAmount.subtract(receiptAmount).abs().compareTo(new BigDecimal("0.01")) <= 0;
        boolean invoiceMatch = receiptAmount.subtract(invoiceAmount).abs().compareTo(new BigDecimal("0.01")) <= 0;

        if (orderMatch && invoiceMatch) {
            match.setMatchResult("MATCH");
        } else {
            match.setMatchResult("MISMATCH");
            StringBuilder diff = new StringBuilder();
            if (!orderMatch) {
                diff.append("订单金额(").append(orderAmount).append(")与入库金额(")
                   .append(receiptAmount).append(")不一致; "); // Order amount vs receipt amount mismatch
            }
            if (!invoiceMatch) {
                diff.append("入库金额(").append(receiptAmount).append(")与发票金额(")
                   .append(invoiceAmount).append(")不一致; "); // Receipt amount vs invoice amount mismatch
            }
            match.setDifferenceDesc(diff.toString());
        }
        matchMapper.insert(match);
        return match;
    }

    /**
     * 按结算单查询三单匹配记录 / Query three-way match records by settlement ID
     */
    public java.util.List<SettlementThreeWayMatch> listMatches(Long settlementId) {
        LambdaQueryWrapper<SettlementThreeWayMatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettlementThreeWayMatch::getSettlementId, settlementId);
        return matchMapper.selectList(wrapper);
    }

    // ==================== 消息推送辅助方法 / Message push helper methods ====================

    /**
     * 异步推送结算事件消息（不阻塞主业务流程） /
     * Async push settlement event message (non-blocking to main business flow)
     */
    @Async
    protected void pushSettlementEvent(SettlementBill bill, String eventName, String title, String content) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("title", title);
            request.put("content", content);
            request.put("type", "SETTLEMENT");
            request.put("relatedId", bill.getId() != null ? bill.getId().toString() : null);
            request.put("relatedType", "SETTLEMENT_BILL");
            request.put("supplierId", bill.getSupplierId());
            messageFeignClient.pushToSupplier(request);
            log.info("Settlement 消息已推送: event={}, billId={}, supplierId={}",
                    eventName, bill.getId(), bill.getSupplierId());
        } catch (Exception e) {
            log.error("Settlement 消息推送失败(Feign调用异常,已降级): event={}, billId={}, error={}",
                    eventName, bill.getId(), e.getMessage());
        }
    }
}
