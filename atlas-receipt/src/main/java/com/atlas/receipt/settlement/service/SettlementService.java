package com.atlas.receipt.settlement.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.entity.Receipt;
import com.atlas.receipt.entity.ReceiptItem;
import com.atlas.receipt.mapper.ReceiptItemMapper;
import com.atlas.receipt.mapper.ReceiptMapper;
import com.atlas.receipt.settlement.entity.SettlementBill;
import com.atlas.receipt.settlement.entity.SettlementItem;
import com.atlas.receipt.settlement.enums.SettlementStatus;
import com.atlas.receipt.service.ReceiptService;
import com.atlas.receipt.settlement.mapper.SettlementBillMapper;
import com.atlas.receipt.settlement.mapper.SettlementItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 结算管理服务 / Settlement management service
 * <p>
 * 核心流程：已收货确认 → 生成结算单 → 提交审批 → 审批通过/驳回 /
 * Core flow: Receipt confirmed → Generate settlement → Submit → Approve/Reject
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementBillMapper settlementBillMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final ReceiptMapper receiptMapper;
    private final ReceiptItemMapper receiptItemMapper;

    /**
     * 基于已收货订单生成结算单 / Generate settlement from confirmed receipt
     *
     * @param receiptId 收货单ID / Receipt ID
     * @param createdBy 创建人 / Created by
     * @return 结算单 / Settlement bill
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementBill generate(Long receiptId, Long createdBy) {
        // 1. 校验收货单存在且已确认 / Validate receipt exists and confirmed
        Receipt receipt = receiptMapper.selectById(receiptId);
        if (receipt == null) {
            throw new BizException(ErrorCode.RECEIPT_NOT_EXIST);
        }
        if (receipt.getStatus() != ReceiptService.STATUS_CONFIRMED) {
            throw new BizException(6003, "收货单未确认，无法生成结算单 / Receipt not confirmed");
        }

        // 2. 校验是否已生成过结算单 / Check if settlement already exists
        Long count = settlementBillMapper.selectCount(
                new LambdaQueryWrapper<SettlementBill>().eq(SettlementBill::getReceiptId, receiptId));
        if (count > 0) {
            throw new BizException(ErrorCode.SETTLEMENT_ALREADY_CONFIRMED);
        }

        // 3. 查询收货明细 / Query receipt items
        List<ReceiptItem> receiptItems = receiptItemMapper.selectList(
                new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, receiptId));

        // 4. 计算结算总金额 / Calculate total amount
        // TODO: 单价应从采购订单或价格主数据获取（Feign 调用 atlas-purchase），当前暂用 0
        // TODO: Unit price should come from PO or price master (Feign to atlas-purchase), currently defaults to 0
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SettlementItem> settlementItems = new ArrayList<>();
        for (ReceiptItem ri : receiptItems) {
            BigDecimal qty = ri.getQualifiedQty() != null ? ri.getQualifiedQty()
                    : (ri.getReceivedQty() != null ? ri.getReceivedQty() : BigDecimal.ZERO);
            // 单价从采购订单获取；如 atlas-purchase 提供 Feign 接口，替换以下逻辑
            // Price from PO; replace when atlas-purchase Feign is available
            BigDecimal price = BigDecimal.ZERO;
            BigDecimal amount = price.multiply(qty);

            SettlementItem si = new SettlementItem();
            si.setMaterialId(ri.getSkuId());
            si.setQuantity(qty);
            si.setUnitPrice(price);
            si.setAmount(amount);
            settlementItems.add(si);
            totalAmount = totalAmount.add(amount);
        }

        // 5. 插入结算单主表 / Insert settlement bill
        SettlementBill bill = new SettlementBill();
        bill.setReceiptId(receiptId);
        bill.setSupplierId(receipt.getSupplierId());
        bill.setTotalAmount(totalAmount);
        bill.setStatus(SettlementStatus.PENDING.getCode());
        bill.setCreatedBy(createdBy);
        settlementBillMapper.insert(bill);

        // 6. 批量插入结算明细 / Batch insert settlement items
        for (SettlementItem si : settlementItems) {
            si.setSettlementId(bill.getId());
            settlementItemMapper.insert(si);
        }

        log.info("生成结算单成功: settlementId={} receiptId={} totalAmount={}", bill.getId(), receiptId, totalAmount);
        return bill;
    }

    /**
     * 结算单详情 / Settlement detail
     */
    public SettlementBill getById(Long id) {
        SettlementBill bill = settlementBillMapper.selectById(id);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }
        return bill;
    }

    /**
     * 分页查询结算单 / Paginated query of settlement bills
     *
     * @param supplierId 供应商ID（可选） / Supplier ID (optional)
     * @param status     状态（可选） / Status (optional)
     * @param startTime  开始时间（可选） / Start time (optional)
     * @param endTime    结束时间（可选） / End time (optional)
     * @param page       当前页 / Current page
     * @param size       每页大小 / Page size
     */
    public Page<SettlementBill> list(Long supplierId, String status,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      int page, int size) {
        LambdaQueryWrapper<SettlementBill> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SettlementBill::getSupplierId, supplierId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SettlementBill::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(SettlementBill::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(SettlementBill::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(SettlementBill::getCreatedAt);
        return settlementBillMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询结算明细 / Query settlement items
     */
    public List<SettlementItem> listItems(Long settlementId) {
        return settlementItemMapper.selectList(
                new LambdaQueryWrapper<SettlementItem>().eq(SettlementItem::getSettlementId, settlementId));
    }

    /**
     * 提交审批 / Submit for approval
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementBill submit(Long id) {
        SettlementBill bill = getById(id);
        if (!SettlementStatus.PENDING.getCode().equals(bill.getStatus())) {
            throw new BizException(6003, "仅待审批状态的结算单可提交审批 / Only PENDING settlement can be submitted");
        }
        // 状态不变，仍为 PENDING，等待审批人操作 / Status remains PENDING, awaiting approver action
        log.info("结算单已提交审批: settlementId={}", id);
        return bill;
    }

    /**
     * 审批通过 / Approve
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementBill approve(Long id) {
        SettlementBill bill = getById(id);
        if (!SettlementStatus.PENDING.getCode().equals(bill.getStatus())) {
            throw new BizException(6003, "仅待审批状态的结算单可审批 / Only PENDING settlement can be approved");
        }
        bill.setStatus(SettlementStatus.APPROVED.getCode());
        settlementBillMapper.updateById(bill);
        log.info("结算单审批通过: settlementId={}", id);

        // TODO: 结算后更新价格主数据 — 通过 Feign 调用 atlas-purchase 的 PUT /price 接口
        // TODO: Update price master data after settlement — Call atlas-purchase PUT /price via Feign
        // FeignClient: com.atlas.purchase.client.PriceMasterClient
        // Method: void updatePrice(Long materialId, BigDecimal newPrice);

        return bill;
    }

    /**
     * 审批驳回 / Reject
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementBill reject(Long id) {
        SettlementBill bill = getById(id);
        if (!SettlementStatus.PENDING.getCode().equals(bill.getStatus())) {
            throw new BizException(6003, "仅待审批状态的结算单可驳回 / Only PENDING settlement can be rejected");
        }
        // 驳回后重置为 PENDING，允许重新提交 / Reset to PENDING for resubmission
        log.info("结算单审批驳回: settlementId={}", id);
        return bill;
    }
}
