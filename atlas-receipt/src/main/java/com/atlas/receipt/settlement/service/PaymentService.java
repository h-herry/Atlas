package com.atlas.receipt.settlement.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.settlement.entity.PaymentRecord;
import com.atlas.receipt.settlement.entity.SettlementBill;
import com.atlas.receipt.settlement.entity.ThreeWayMatch;
import com.atlas.receipt.settlement.enums.MatchStatus;
import com.atlas.receipt.settlement.enums.PaymentStatus;
import com.atlas.receipt.settlement.enums.SettlementStatus;
import com.atlas.receipt.settlement.mapper.PaymentRecordMapper;
import com.atlas.receipt.settlement.mapper.SettlementBillMapper;
import com.atlas.receipt.settlement.mapper.ThreeWayMatchMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款管理服务 / Payment management service
 * <p>
 * 流程：创建付款申请 → 审批付款 → 确认支付 / 
 * Flow: Create payment → Approve → Confirm pay
 * <p>
 * 前置条件：三单匹配必须通过（MATCHED）。 /
 * Precondition: Three-way match must be MATCHED.
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final ThreeWayMatchMapper threeWayMatchMapper;

    /**
     * 创建付款申请 / Create payment application
     * <p>
     * 前置条件：三单匹配已通过 / Precondition: three-way match is MATCHED
     *
     * @param settlementId 结算单ID / Settlement ID
     * @param payAmount    付款金额 / Payment amount
     * @param payMethod    付款方式 / Payment method
     * @param createdBy    创建人 / Created by
     * @param remark       备注 / Remark
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord create(Long settlementId, BigDecimal payAmount, String payMethod,
                                 Long createdBy, String remark) {
        // 1. 校验结算单存在 / Validate settlement exists
        SettlementBill bill = settlementBillMapper.selectById(settlementId);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }

        // 2. 校验三单匹配已通过 / Validate three-way match passed
        ThreeWayMatch match = threeWayMatchMapper.selectOne(
                new LambdaQueryWrapper<ThreeWayMatch>().eq(ThreeWayMatch::getSettlementId, settlementId));
        if (match == null || !MatchStatus.MATCHED.getCode().equals(match.getMatchStatus())) {
            throw new BizException(6003, "三单匹配未通过，无法创建付款 / Three-way match not passed");
        }

        // 3. 校验付款金额不超过结算金额 / Validate pay amount <= settlement amount
        if (payAmount.compareTo(bill.getTotalAmount()) > 0) {
            throw new BizException(6003, "付款金额不得超过结算金额 / Pay amount exceeds settlement amount");
        }

        // 4. 创建付款记录 / Create payment record
        PaymentRecord record = new PaymentRecord();
        record.setSettlementId(settlementId);
        record.setPayAmount(payAmount);
        record.setPayMethod(payMethod);
        record.setStatus(PaymentStatus.PENDING.getCode());
        record.setRemark(remark);
        record.setCreatedBy(createdBy);
        paymentRecordMapper.insert(record);

        log.info("创建付款申请: paymentId={} settlementId={} payAmount={}", record.getId(), settlementId, payAmount);
        return record;
    }

    /**
     * 审批付款 / Approve payment
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord approve(Long id, Long approvedBy) {
        PaymentRecord record = getById(id);
        if (!PaymentStatus.PENDING.getCode().equals(record.getStatus())) {
            throw new BizException(6003, "仅待审批的付款可审批 / Only PENDING payment can be approved");
        }

        record.setStatus(PaymentStatus.APPROVED.getCode());
        record.setApprovedBy(approvedBy);
        record.setApprovedAt(LocalDateTime.now());
        paymentRecordMapper.updateById(record);

        log.info("付款审批通过: paymentId={} approvedBy={}", id, approvedBy);
        return record;
    }

    /**
     * 确认支付 / Confirm payment
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord pay(Long id, Long paidBy) {
        PaymentRecord record = getById(id);
        if (!PaymentStatus.APPROVED.getCode().equals(record.getStatus())) {
            throw new BizException(6003, "仅已审批的付款可执行支付 / Only APPROVED payment can be paid");
        }

        record.setStatus(PaymentStatus.PAID.getCode());
        record.setPaidBy(paidBy);
        record.setPayTime(LocalDateTime.now());
        paymentRecordMapper.updateById(record);

        // 更新结算单状态为已付款 / Update settlement status to PAID
        SettlementBill bill = settlementBillMapper.selectById(record.getSettlementId());
        if (bill != null) {
            bill.setStatus(SettlementStatus.PAID.getCode());
            settlementBillMapper.updateById(bill);
        }

        log.info("付款确认支付: paymentId={} paidBy={}", id, paidBy);
        return record;
    }

    /**
     * 按ID查询付款记录 / Query payment by ID
     */
    public PaymentRecord getById(Long id) {
        PaymentRecord record = paymentRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(400, "付款记录不存在 / Payment record not found");
        }
        return record;
    }

    /**
     * 分页查询付款记录 / Paginated query of payment records
     *
     * @param settlementId 结算单ID（可选） / Settlement ID (optional)
     * @param status       状态（可选） / Status (optional)
     * @param page         当前页 / Current page
     * @param size         每页大小 / Page size
     */
    public Page<PaymentRecord> list(Long settlementId, String status, int page, int size) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        if (settlementId != null) {
            wrapper.eq(PaymentRecord::getSettlementId, settlementId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PaymentRecord::getStatus, status);
        }
        wrapper.orderByDesc(PaymentRecord::getCreatedAt);
        return paymentRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
