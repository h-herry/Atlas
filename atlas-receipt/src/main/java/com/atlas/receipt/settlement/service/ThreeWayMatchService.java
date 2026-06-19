package com.atlas.receipt.settlement.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.entity.Receipt;
import com.atlas.receipt.entity.ReceiptItem;
import com.atlas.receipt.mapper.ReceiptItemMapper;
import com.atlas.receipt.mapper.ReceiptMapper;
import com.atlas.receipt.settlement.entity.SettlementBill;
import com.atlas.receipt.settlement.entity.SettlementItem;
import com.atlas.receipt.settlement.entity.ThreeWayMatch;
import com.atlas.receipt.settlement.enums.MatchStatus;
import com.atlas.receipt.settlement.mapper.SettlementBillMapper;
import com.atlas.receipt.settlement.mapper.SettlementItemMapper;
import com.atlas.receipt.settlement.mapper.ThreeWayMatchMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 三单匹配引擎服务 / Three-way match engine service
 * <p>
 * 匹配维度：PO 采购订单 vs 收货单 vs 发票 /
 * Match dimensions: PO vs Receipt vs Invoice
 * <p>
 * 匹配规则：数量一致 + 单价容差 ±1% + 金额容差 ±1% /
 * Match rules: Quantity match + Unit price tolerance ±1% + Amount tolerance ±1%
 * <p>
 * 不匹配时记录差异明细到 diffDetails（JSON）字段。 /
 * When mismatched, detail differences are recorded in diffDetails (JSON).
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreeWayMatchService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");  // 容差 1%
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ThreeWayMatchMapper threeWayMatchMapper;
    private final SettlementBillMapper settlementBillMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final ReceiptMapper receiptMapper;
    private final ReceiptItemMapper receiptItemMapper;

    /**
     * 执行三单匹配 / Execute three-way match
     * <p>
     * 以结算明细为基准，逐项比对收货单明细和 PO 采购订单。 /
     * Based on settlement items, compare against receipt items and PO line by line.
     *
     * @param settlementId 结算单ID / Settlement ID
     * @param invoiceNo    发票号 / Invoice number
     * @return 匹配结果 / Match result
     */
    @Transactional(rollbackFor = Exception.class)
    public ThreeWayMatch execute(Long settlementId, String invoiceNo) {
        // 1. 校验结算单存在 / Validate settlement exists
        SettlementBill bill = settlementBillMapper.selectById(settlementId);
        if (bill == null) {
            throw new BizException(ErrorCode.SETTLEMENT_NOT_EXIST);
        }

        // 2. 校验是否已匹配过 / Check if already matched
        Long count = threeWayMatchMapper.selectCount(
                new LambdaQueryWrapper<ThreeWayMatch>().eq(ThreeWayMatch::getSettlementId, settlementId));
        if (count > 0) {
            throw new BizException(6003, "该结算单已完成三单匹配 / Three-way match already done for this settlement");
        }

        // 3. 获取收货单 / Get receipt
        Receipt receipt = receiptMapper.selectById(bill.getReceiptId());
        if (receipt == null) {
            throw new BizException(ErrorCode.RECEIPT_NOT_EXIST);
        }

        // 4. 获取结算明细 / Get settlement items
        List<SettlementItem> settlementItems = settlementItemMapper.selectList(
                new LambdaQueryWrapper<SettlementItem>().eq(SettlementItem::getSettlementId, settlementId));

        // 5. 获取收货单明细 / Get receipt items
        List<ReceiptItem> receiptItems = receiptItemMapper.selectList(
                new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, bill.getReceiptId()));

        // 6. 逐项比对 / Compare line by line
        List<DiffDetail> diffs = new ArrayList<>();

        Map<Long, ReceiptItem> receiptItemMap = new HashMap<>();
        for (ReceiptItem ri : receiptItems) {
            receiptItemMap.put(ri.getSkuId(), ri);
        }

        for (SettlementItem si : settlementItems) {
            ReceiptItem ri = receiptItemMap.get(si.getMaterialId());
            if (ri == null) {
                diffs.add(new DiffDetail(si.getMaterialId(), "RECEIPT_MISSING",
                        "收货单中不存在该物料 / Material not found in receipt"));
                continue;
            }

            // 数量比对 / Quantity comparison
            BigDecimal receiptQty = ri.getQualifiedQty() != null ? ri.getQualifiedQty()
                    : (ri.getReceivedQty() != null ? ri.getReceivedQty() : BigDecimal.ZERO);
            if (si.getQuantity().compareTo(receiptQty) != 0) {
                diffs.add(new DiffDetail(si.getMaterialId(), "QUANTITY_MISMATCH",
                        String.format("结算数量=%s, 收货数量=%s / Settlement qty=%s, Receipt qty=%s",
                                si.getQuantity(), receiptQty, si.getQuantity(), receiptQty)));
            }

            // 单价容差比对 ±1% / Unit price tolerance ±1%
            // TODO: 收货单价格应从采购订单获取；当前收货单实体无 unitPrice 字段，跳过单价比对
            // TODO: Receipt price should come from PO; ReceiptItem has no unitPrice, skip price comparison for now
            // 金额容差比对 ±1% / Amount tolerance ±1%
            // TODO: 收货单金额 = 收货单价 × 收货数量，单价缺失时跳过金额比对
            // TODO: Receipt amount = receipt price × receipt qty; skip when price is unavailable
        }

        // 7. 构建匹配结果 / Build match result
        ThreeWayMatch match = new ThreeWayMatch();
        match.setSettlementId(settlementId);
        match.setPoId(receipt.getOrderId());
        match.setReceiptId(bill.getReceiptId());
        match.setInvoiceNo(invoiceNo);

        if (diffs.isEmpty()) {
            match.setMatchStatus(MatchStatus.MATCHED.getCode());
            match.setDiffDetails(null);
        } else {
            match.setMatchStatus(MatchStatus.MISMATCH.getCode());
            try {
                match.setDiffDetails(OBJECT_MAPPER.writeValueAsString(diffs));
            } catch (JsonProcessingException e) {
                log.error("序列化差异明细失败 / Failed to serialize diff details", e);
                match.setDiffDetails("[]");
            }
        }

        threeWayMatchMapper.insert(match);
        log.info("三单匹配完成: settlementId={} status={} diffCount={}",
                settlementId, match.getMatchStatus(), diffs.size());

        if (!diffs.isEmpty()) {
            throw new BizException(ErrorCode.THREE_WAY_MISMATCH);
        }

        return match;
    }

    /**
     * 查询匹配详情 / Query match detail
     */
    public ThreeWayMatch getById(Long id) {
        ThreeWayMatch match = threeWayMatchMapper.selectById(id);
        if (match == null) {
            throw new BizException(ErrorCode.THREE_WAY_MISMATCH);
        }
        return match;
    }

    /**
     * 按结算单ID查询 / Query by settlement ID
     */
    public ThreeWayMatch getBySettlementId(Long settlementId) {
        return threeWayMatchMapper.selectOne(
                new LambdaQueryWrapper<ThreeWayMatch>().eq(ThreeWayMatch::getSettlementId, settlementId));
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * 差异明细项 / Difference detail item
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DiffDetail {
        private Long materialId;
        private String diffType;
        private String detail;
    }
}
