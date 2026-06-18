package com.atlas.receipt.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.mq.producer.AbstractMessageProducer;
import com.atlas.receipt.entity.Receipt;
import com.atlas.receipt.entity.ReceiptItem;
import com.atlas.receipt.entity.ReceiptOutbox;
import com.atlas.receipt.mapper.ReceiptItemMapper;
import com.atlas.receipt.mapper.ReceiptMapper;
import com.atlas.receipt.mapper.ReceiptOutboxMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收货管理核心业务服务 / Receipt management core business service
 * <p>
 * 提供收货单创建、质检、确认收货（含 RocketMQ 通知入库）及分页查询。 /
 * Provides receipt creation, quality inspection, confirm receipt (with RocketMQ stock notification), and paginated queries.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptMapper receiptMapper;
    private final ReceiptItemMapper receiptItemMapper;
    private final ReceiptOutboxMapper outboxMapper;
    private final ReceiptConfirmProducer receiptConfirmProducer;
    private final ReceiptOutboxService outboxService;

    /** 收货单状态常量 / Receipt status constants */
    public static final int STATUS_PENDING = 0;    // 待收货 / Pending
    public static final int STATUS_PARTIAL = 1;    // 部分收货 / Partial
    public static final int STATUS_FULL = 2;        // 全部收货 / Full
    public static final int STATUS_INSPECTING = 3;  // 质检中 / Inspecting
    public static final int STATUS_CONFIRMED = 4;   // 已入库 / Confirmed

    /** 质检结果常量 / Inspection result constants */
    public static final String CHECK_PASS = "PASS";
    public static final String CHECK_FAIL = "FAIL";

    /**
     * 根据采购订单生成收货单 / Generate receipt from purchase order
     *
     * @param orderId     采购订单ID / Purchase order ID
     * @param supplierId  供应商ID / Supplier ID
     * @param warehouseId 收货仓库ID / Receiving warehouse ID
     * @param items       收货明细（skuId + orderQty） / Receipt items (skuId + orderQty)
     * @param createdBy   创建人 / Created by
     * @return 收货单 / Receipt
     */
    @Transactional(rollbackFor = Exception.class)
    public Receipt createReceipt(Long orderId, Long supplierId, Long warehouseId,
                                  List<ReceiptItemRequest> items, Long createdBy) {
        // 校验是否已存在 / Check if already exists
        Long count = receiptMapper.selectCount(
                new LambdaQueryWrapper<Receipt>().eq(Receipt::getOrderId, orderId));
        if (count > 0) {
            throw new BizException(ErrorCode.RECEIPT_DUPLICATE);
        }

        // 生成收货单编号 / Generate receipt number
        String receiptNo = "RCP" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        // 插入主表 / Insert master record
        Receipt receipt = new Receipt();
        receipt.setReceiptNo(receiptNo);
        receipt.setOrderId(orderId);
        receipt.setWarehouseId(warehouseId);
        receipt.setSupplierId(supplierId);
        receipt.setStatus(STATUS_PENDING);
        receipt.setCreatedBy(createdBy);
        receiptMapper.insert(receipt);

        // 插入明细 / Insert items
        for (ReceiptItemRequest itemReq : items) {
            ReceiptItem item = new ReceiptItem();
            item.setReceiptId(receipt.getId());
            item.setSkuId(itemReq.getSkuId());
            item.setOrderQty(itemReq.getOrderQty());
            item.setReceivedQty(BigDecimal.ZERO);
            item.setQualifiedQty(BigDecimal.ZERO);
            receiptItemMapper.insert(item);
        }

        log.info("创建收货单成功: receiptNo={} orderId={} itemCount={}", receiptNo, orderId, items.size());
        return receipt;
    }

    /**
     * 质检 / Quality inspection
     *
     * @param receiptId   收货单ID / Receipt ID
     * @param checkResult 质检结果（PASS/FAIL） / Inspection result (PASS/FAIL)
     * @param inspectorId 质检人 / Inspector ID
     * @return 更新后的收货单 / Updated receipt
     */
    @Transactional(rollbackFor = Exception.class)
    public Receipt qualityCheck(Long receiptId, String checkResult, Long inspectorId) {
        Receipt receipt = receiptMapper.selectById(receiptId);
        if (receipt == null) {
            throw new BizException(ErrorCode.RECEIPT_NOT_EXIST);
        }

        receipt.setInspectorId(inspectorId);
        receipt.setInspectedAt(LocalDateTime.now());

        if (CHECK_PASS.equals(checkResult)) {
            // 质检通过 → 更新状态为「全部收货」，下一步可确认入库 / Pass → set status to FULL, ready for confirm
            receipt.setStatus(STATUS_FULL);

            // 更新所有明细的合格数量 = 实收数量 / Set qualified qty = received qty for all items
            List<ReceiptItem> items = receiptItemMapper.selectList(
                    new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, receiptId));
            for (ReceiptItem item : items) {
                item.setQualifiedQty(item.getReceivedQty());
                item.setRejectReason(null);
                receiptItemMapper.updateById(item);
            }

            log.info("质检通过: receiptNo={} inspectorId={}", receipt.getReceiptNo(), inspectorId);
        } else {
            // 质检不通过 → 记录不合格信息 / Fail → record rejection info
            receipt.setStatus(STATUS_INSPECTING);
            log.info("质检不通过: receiptNo={} inspectorId={}", receipt.getReceiptNo(), inspectorId);
        }

        receiptMapper.updateById(receipt);
        return receipt;
    }

    /**
     * 确认收货 / Confirm receipt
     * <p>
     * 流程： / Flow:
     * <ol>
     *   <li>写入本地消息表（独立事务，不受后续回滚影响） / Write to outbox (independent tx)</li>
     *   <li>更新收货单状态为 CONFIRMED（参与当前事务） / Update status to CONFIRMED (in current tx)</li>
     *   <li>发送 RocketMQ 消息通知 inventory 入库 / Send RocketMQ message to notify inventory</li>
     *   <li>MQ 发送失败则抛出 BizException 触发事务回滚，
     *       outbox 已独立持久化，由 ReceiptOutboxService 定时补偿重试 / 
     *       MQ failure triggers rollback; outbox already persisted for scheduled retry</li>
     * </ol>
     *
     * @param receiptId 收货单ID / Receipt ID
     * @return 确认后的收货单 / Confirmed receipt
     */
    @GlobalTransactional(timeoutMills = 300000)
    @Transactional(rollbackFor = Exception.class)
    public Receipt confirmReceipt(Long receiptId) {
        Receipt receipt = receiptMapper.selectById(receiptId);
        if (receipt == null) {
            throw new BizException(ErrorCode.RECEIPT_NOT_EXIST);
        }

        if (receipt.getStatus() != STATUS_FULL && receipt.getStatus() != STATUS_PARTIAL) {
            throw new BizException(6003, "当前状态不允许确认收货，请先完成质检");
        }

        // 查询明细 / Query items
        List<ReceiptItem> items = receiptItemMapper.selectList(
                new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, receiptId));

        // 构造消息体 / Build message body
        InventoryRestockMessage message = new InventoryRestockMessage();
        message.setOrderNo(receipt.getReceiptNo());
        message.setWarehouseId(receipt.getWarehouseId());
        message.setItems(items.stream().map(item -> {
            InventoryRestockMessage.RestockItem ri = new InventoryRestockMessage.RestockItem();
            ri.setSkuId(item.getSkuId());
            ri.setQty(item.getQualifiedQty() != null ? item.getQualifiedQty() : item.getReceivedQty());
            return ri;
        }).collect(Collectors.toList()));

        // 先写入本地消息表（独立事务，不受后续回滚影响，补偿重试时仍可追溯） / Write outbox first (independent tx for traceability)
        ReceiptOutbox outbox = new ReceiptOutbox();
        outbox.setReceiptId(receipt.getId());
        outbox.setMessageTopic(receiptConfirmProducer.topic());
        outbox.setMessageTag(receiptConfirmProducer.tags());
        outbox.setMessageBody(receipt.getReceiptNo());
        outbox.setRetryCount(0);
        outbox.setStatus(0); // 待发送（发送成功后更新为1） / Pending (updated to 1 after send)
        outbox.setNextRetryTime(LocalDateTime.now().plusMinutes(1));
        outboxService.saveOutbox(outbox);

        // 更新收货状态（参与当前事务，失败则回滚） / Update status (in current tx, rollback on failure)
        receipt.setStatus(STATUS_CONFIRMED);
        receipt.setUpdatedAt(LocalDateTime.now());
        receiptMapper.updateById(receipt);

        // 发送 MQ（失败则抛出异常触发事务回滚，outbox 已独立持久化，由定时补偿重试） / Send MQ (failure triggers rollback; outbox for retry)
        try {
            receiptConfirmProducer.sendAsync(receipt.getReceiptNo(), message);
            outbox.setStatus(1); // 已发送 / Sent
            outboxMapper.updateById(outbox);
            log.info("收货确认消息已发送: receiptNo={} itemCount={}", receipt.getReceiptNo(), items.size());
        } catch (Exception e) {
            log.error("发送收货确认消息失败，事务将回滚: receiptNo={}", receipt.getReceiptNo(), e);
            throw new BizException(ErrorCode.MQ_SEND_FAILED);
        }

        return receipt;
    }

    /**
     * 分页查询收货单（支持按订单号/状态筛选） / Paginated query (supports orderNo/status filter)
     *
     * @param orderId 采购订单ID（可选） / Purchase order ID (optional)
     * @param status  状态（可选） / Status (optional)
     * @param page    当前页 / Current page
     * @param size    每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<Receipt> page(Long orderId, Integer status, int page, int size) {
        LambdaQueryWrapper<Receipt> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(Receipt::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(Receipt::getStatus, status);
        }
        wrapper.orderByDesc(Receipt::getCreatedAt);
        return receiptMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 按ID查询收货单 / Query receipt by ID
     */
    public Receipt getById(Long receiptId) {
        Receipt receipt = receiptMapper.selectById(receiptId);
        if (receipt == null) {
            throw new BizException(ErrorCode.RECEIPT_NOT_EXIST);
        }
        return receipt;
    }

    /**
     * 查询收货单明细 / Query receipt items
     */
    public List<ReceiptItem> listItems(Long receiptId) {
        return receiptItemMapper.selectList(
                new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, receiptId));
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * 收货单明细创建请求 / Receipt item creation request
     */
    @lombok.Data
    public static class ReceiptItemRequest {
        private Long skuId;
        private BigDecimal orderQty;
    }

    /**
     * 收货确认 — RocketMQ 消息体 / Receipt confirm — RocketMQ message body
     */
    @lombok.Data
    public static class InventoryRestockMessage {
        private String orderNo;
        private Long warehouseId;
        private List<RestockItem> items;

        @lombok.Data
        public static class RestockItem {
            private Long skuId;
            private BigDecimal qty;
        }
    }

    /**
     * 收货确认消息生产者 / Receipt confirm message producer
     */
    @Slf4j
    @Service
    public static class ReceiptConfirmProducer extends AbstractMessageProducer {
        @Override
        protected String topic() {
            return "atlas-receipt-confirm";
        }

        @Override
        protected String tags() {
            return "inventory-restock";
        }
    }
}
