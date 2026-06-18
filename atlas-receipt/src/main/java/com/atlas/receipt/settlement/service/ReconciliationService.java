package com.atlas.receipt.settlement.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.settlement.entity.ReconciliationRecord;
import com.atlas.receipt.settlement.entity.SettlementBill;
import com.atlas.receipt.settlement.enums.MatchStatus;
import com.atlas.receipt.settlement.enums.SettlementStatus;
import com.atlas.receipt.settlement.mapper.ReconciliationRecordMapper;
import com.atlas.receipt.settlement.mapper.SettlementBillMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 对账管理服务 / Reconciliation management service
 * <p>
 * 流程：发起对账 → 企业端确认 → 供应商确认 / Flow: Initiate → Enterprise confirm → Supplier confirm
 * <p>
 * 双方确认一致后状态为 MATCHED，任一方有异议则记录 disputeReason。 /
 * If both parties confirm, status becomes MATCHED; if either party disputes, disputeReason is recorded.
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationRecordMapper reconciliationRecordMapper;
    private final SettlementBillMapper settlementBillMapper;

    /**
     * 发起对账 / Initiate reconciliation
     * <p>
     * 前提：结算单必须已审批 / Precondition: settlement must be APPROVED
     *
     * @param settlementId 结算单ID / Settlement ID
     * @return 对账记录 / Reconciliation record
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationRecord initiate(Long settlementId) {
        // 1. 校验结算单存在且已审批 / Validate settlement exists and approved
        SettlementBill bill = settlementBillMapper.selectById(settlementId);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        if (!SettlementStatus.APPROVED.getCode().equals(bill.getStatus())) {
            throw new BizException(6003, "结算单未审批通过，无法发起对账 / Settlement not approved");
        }

        // 2. 校验是否已有对账记录 / Check if reconciliation already exists
        Long count = reconciliationRecordMapper.selectCount(
                new LambdaQueryWrapper<ReconciliationRecord>()
                        .eq(ReconciliationRecord::getSettlementId, settlementId));
        if (count > 0) {
            throw new BizException(ErrorCode.RECONCILIATION_ALREADY_CONFIRMED);
        }

        // 3. 创建对账记录 / Create reconciliation record
        ReconciliationRecord record = new ReconciliationRecord();
        record.setSettlementId(settlementId);
        record.setEnterpriseConfirm(false);
        record.setSupplierConfirm(false);
        record.setStatus(MatchStatus.PENDING.getCode());
        reconciliationRecordMapper.insert(record);

        log.info("对账发起成功: settlementId={} reconciliationId={}", settlementId, record.getId());
        return record;
    }

    /**
     * 企业端确认 / Enterprise confirm
     *
     * @param id       对账记录ID / Reconciliation record ID
     * @param userId   确认人 / User ID
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationRecord enterpriseConfirm(Long id, Long userId) {
        ReconciliationRecord record = getById(id);
        if (Boolean.TRUE.equals(record.getEnterpriseConfirm())) {
            throw new BizException(6003, "企业端已确认，不可重复操作 / Enterprise already confirmed");
        }

        record.setEnterpriseConfirm(true);
        record.setEnterpriseConfirmedBy(userId);
        record.setEnterpriseConfirmedAt(LocalDateTime.now());

        // 双方均确认 → 对账完成 / Both confirmed → reconciliation complete
        if (Boolean.TRUE.equals(record.getSupplierConfirm())) {
            record.setStatus(MatchStatus.MATCHED.getCode());
            // 更新结算单状态为已对账 / Update settlement status to RECONCILED
            updateSettlementStatus(record.getSettlementId(), SettlementStatus.RECONCILED);
        }

        reconciliationRecordMapper.updateById(record);
        log.info("企业端确认对账: reconciliationId={} userId={}", id, userId);
        return record;
    }

    /**
     * 供应商确认 / Supplier confirm
     *
     * @param id       对账记录ID / Reconciliation record ID
     * @param userId   确认人 / User ID
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationRecord supplierConfirm(Long id, Long userId) {
        ReconciliationRecord record = getById(id);
        if (Boolean.TRUE.equals(record.getSupplierConfirm())) {
            throw new BizException(6003, "供应商已确认，不可重复操作 / Supplier already confirmed");
        }

        record.setSupplierConfirm(true);
        record.setSupplierConfirmedBy(userId);
        record.setSupplierConfirmedAt(LocalDateTime.now());

        // 双方均确认 → 对账完成 / Both confirmed → reconciliation complete
        if (Boolean.TRUE.equals(record.getEnterpriseConfirm())) {
            record.setStatus(MatchStatus.MATCHED.getCode());
            // 更新结算单状态为已对账 / Update settlement status to RECONCILED
            updateSettlementStatus(record.getSettlementId(), SettlementStatus.RECONCILED);
        }

        reconciliationRecordMapper.updateById(record);
        log.info("供应商确认对账: reconciliationId={} userId={}", id, userId);
        return record;
    }

    /**
     * 供应商异议 / Supplier dispute
     *
     * @param id     对账记录ID / Reconciliation record ID
     * @param reason 异议原因 / Dispute reason
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationRecord dispute(Long id, String reason) {
        ReconciliationRecord record = getById(id);
        if (Boolean.TRUE.equals(record.getSupplierConfirm())) {
            throw new BizException(6003, "供应商已确认，不可再提出异议 / Supplier already confirmed");
        }

        record.setDisputeReason(reason);
        record.setStatus(MatchStatus.MISMATCH.getCode());
        reconciliationRecordMapper.updateById(record);

        log.info("供应商提出对账异议: reconciliationId={} reason={}", id, reason);
        return record;
    }

    /**
     * 按ID查询对账记录 / Query reconciliation by ID
     */
    public ReconciliationRecord getById(Long id) {
        ReconciliationRecord record = reconciliationRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ErrorCode.RECONCILIATION_NOT_EXIST);
        }
        return record;
    }

    /**
     * 按结算单ID查询对账记录 / Query reconciliation by settlement ID
     */
    public ReconciliationRecord getBySettlementId(Long settlementId) {
        return reconciliationRecordMapper.selectOne(
                new LambdaQueryWrapper<ReconciliationRecord>()
                        .eq(ReconciliationRecord::getSettlementId, settlementId));
    }

    /**
     * 更新结算单状态 / Update settlement status
     */
    private void updateSettlementStatus(Long settlementId, SettlementStatus status) {
        SettlementBill bill = settlementBillMapper.selectById(settlementId);
        if (bill != null) {
            bill.setStatus(status.getCode());
            settlementBillMapper.updateById(bill);
        }
    }
}
