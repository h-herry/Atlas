package com.atlas.receipt.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.entity.ReceivingRecord;
import com.atlas.receipt.mapper.ReceivingRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货质检联动核心业务服务 / Receiving & quality inspection linkage core business service
 * <p>
 * 到货登记 → 自动触发质检申请 → 质检完成回写。与 quality 模块联动： /
 * Arrival registration → auto-trigger inspection → inspection completion callback. Linked with quality module:
 * 收货 → 报检 → IQC 检验 → 合格入库 / 不合格退货 /
 * Receiving → inspection request → IQC → accept to stock / reject & return.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivingService {

    private final ReceivingRecordMapper receivingRecordMapper;

    /** 状态常量 / Status constants */
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_INSPECTING = "INSPECTING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    /** 质检结果常量 / Inspection result constants */
    public static final String INSPECTION_PASS = "PASS";
    public static final String INSPECTION_FAIL = "REJECT";

    /**
     * 到货登记 / Register arrival
     *
     * @param asnId      ASN ID（可为空） / ASN ID (nullable)
     * @param orderId    采购订单ID / Purchase order ID
     * @param supplierId 供应商ID / Supplier ID
     * @param warehouseId 仓库ID / Warehouse ID
     * @param receiverId 收货人ID / Receiver ID
     * @return 收货记录 / Receiving record
     */
    @Transactional(rollbackFor = Exception.class)
    public ReceivingRecord registerArrival(Long asnId, Long orderId, Long supplierId,
                                            Long warehouseId, Long receiverId) {
        String receiveNo = "RCV" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        ReceivingRecord record = new ReceivingRecord();
        record.setReceiveNo(receiveNo);
        record.setAsnId(asnId);
        record.setOrderId(orderId);
        record.setSupplierId(supplierId);
        record.setWarehouseId(warehouseId);
        record.setReceiveDate(LocalDateTime.now());
        record.setReceiverId(receiverId);
        record.setInspectionTriggered(0);
        record.setStatus(STATUS_RECEIVED);
        receivingRecordMapper.insert(record);

        log.info("到货登记完成: receiveNo={} orderId={} asnId={}", receiveNo, orderId, asnId);
        return record;
    }

    /**
     * 触发质检申请（收货 → 报检） / Trigger inspection request (receiving → inspection request)
     *
     * @param receivingId 收货记录ID / Receiving record ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void triggerInspection(Long receivingId) {
        ReceivingRecord record = receivingRecordMapper.selectById(receivingId);
        if (record == null) {
            throw new BizException(6301, "收货记录不存在 / Receiving record not found");
        }
        if (record.getInspectionTriggered() != null && record.getInspectionTriggered() == 1) {
            log.warn("质检已触发，跳过重复触发: receiveNo={}", record.getReceiveNo());
            return;
        }
        record.setInspectionTriggered(1);
        record.setStatus(STATUS_INSPECTING);
        receivingRecordMapper.updateById(record);

        // 注：实际项目中此处通过 Feign/MQ 调用 quality 模块创建 IQC 检验单 /
        // Note: In production, call quality module via Feign/MQ to create IQC inspection order
        log.info("质检申请已触发: receiveNo={} orderId={}", record.getReceiveNo(), record.getOrderId());
    }

    /**
     * 质检完成回写（合格入库 / 不合格退货） / Inspection completion callback (accept to stock / reject & return)
     *
     * @param receivingId 收货记录ID / Receiving record ID
     * @param inspectionId 检验单ID / Inspection ID
     * @param result      质检结果（PASS/REJECT） / Inspection result (PASS/REJECT)
     */
    @Transactional(rollbackFor = Exception.class)
    public void inspectionCallback(Long receivingId, Long inspectionId, String result) {
        ReceivingRecord record = receivingRecordMapper.selectById(receivingId);
        if (record == null) {
            throw new BizException(6301, "收货记录不存在 / Receiving record not found");
        }

        record.setInspectionId(inspectionId);
        record.setInspectionResult(result);

        if (INSPECTION_PASS.equals(result)) {
            record.setStatus(STATUS_ACCEPTED);
            log.info("质检通过，合格入库: receiveNo={} inspectionId={}", record.getReceiveNo(), inspectionId);
        } else {
            record.setStatus(STATUS_REJECTED);
            log.info("质检不通过，退货处理: receiveNo={} inspectionId={}", record.getReceiveNo(), inspectionId);
        }
        receivingRecordMapper.updateById(record);
    }

    /**
     * 按 ID 查询 / Query by ID
     */
    public ReceivingRecord getById(Long receivingId) {
        ReceivingRecord record = receivingRecordMapper.selectById(receivingId);
        if (record == null) {
            throw new BizException(6301, "收货记录不存在 / Receiving record not found");
        }
        return record;
    }

    /**
     * 分页查询收货记录（支持按订单/状态筛选） / Paginated query (supports order/status filter)
     */
    public Page<ReceivingRecord> page(Long orderId, String status, int page, int size) {
        LambdaQueryWrapper<ReceivingRecord> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(ReceivingRecord::getOrderId, orderId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ReceivingRecord::getStatus, status);
        }
        wrapper.orderByDesc(ReceivingRecord::getCreatedAt);
        return receivingRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 按日期查询当日到货记录 / Query today's arrival records
     */
    public List<ReceivingRecord> listByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return receivingRecordMapper.selectList(
                new LambdaQueryWrapper<ReceivingRecord>()
                        .between(ReceivingRecord::getReceiveDate, startDate, startDate != null ? startDate.plusDays(1) : null)
                        .orderByDesc(ReceivingRecord::getReceiveDate));
    }
}
