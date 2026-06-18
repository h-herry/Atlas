package com.atlas.receipt.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.receipt.entity.AsnItem;
import com.atlas.receipt.entity.AsnRecord;
import com.atlas.receipt.mapper.AsnItemMapper;
import com.atlas.receipt.mapper.AsnRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ASN 预先发货通知核心业务服务 / ASN (Advanced Shipping Notice) core business service
 * <p>
 * 供应商创建 ASN → 采购方确认收货。 /
 * Supplier creates ASN → buyer confirms receipt.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsnService {

    private final AsnRecordMapper asnRecordMapper;
    private final AsnItemMapper asnItemMapper;

    /** ASN 状态常量 / ASN status constants */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_ARRIVED = "ARRIVED";
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /**
     * 供应商创建 ASN / Supplier creates ASN
     *
     * @param orderId            采购订单ID / Purchase order ID
     * @param supplierId         供应商ID / Supplier ID
     * @param expectedArrivalDate 预计到货日 / Expected arrival date
     * @param shipDate           实际发货日 / Actual ship date
     * @param carrier            承运商 / Carrier
     * @param trackingNo         物流单号 / Tracking number
     * @param items              ASN 明细 / ASN items
     * @param createdBy          创建人 / Created by
     * @return ASN 记录 / ASN record
     */
    @Transactional(rollbackFor = Exception.class)
    public AsnRecord createAsn(Long orderId, Long supplierId, LocalDate expectedArrivalDate,
                                LocalDate shipDate, String carrier, String trackingNo,
                                List<AsnItemRequest> items, Long createdBy) {
        // 生成 ASN 单号 / Generate ASN number
        String asnNo = "ASN" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        // 插入 ASN 主表 / Insert ASN master
        AsnRecord asn = new AsnRecord();
        asn.setAsnNo(asnNo);
        asn.setOrderId(orderId);
        asn.setSupplierId(supplierId);
        asn.setExpectedArrivalDate(expectedArrivalDate);
        asn.setShipDate(shipDate);
        asn.setCarrier(carrier);
        asn.setTrackingNo(trackingNo);
        asn.setStatus(STATUS_CREATED);
        asn.setCreatedBy(createdBy);
        asnRecordMapper.insert(asn);

        // 插入 ASN 明细 / Insert ASN items
        if (items != null && !items.isEmpty()) {
            for (AsnItemRequest itemReq : items) {
                AsnItem item = new AsnItem();
                item.setAsnId(asn.getId());
                item.setMaterialId(itemReq.getMaterialId());
                item.setMaterialName(itemReq.getMaterialName());
                item.setQuantity(itemReq.getQuantity());
                item.setUnit(itemReq.getUnit());
                item.setBatchNo(itemReq.getBatchNo());
                item.setPackagingType(itemReq.getPackagingType());
                item.setPackageCount(itemReq.getPackageCount());
                asnItemMapper.insert(item);
            }
        }

        log.info("ASN 创建成功: asnNo={} orderId={} supplierId={} itemCount={}",
                asnNo, orderId, supplierId, items != null ? items.size() : 0);
        return asn;
    }

    /**
     * 更新 ASN 状态 / Update ASN status
     *
     * @param asnId ASN ID / ASN ID
     * @param status 目标状态 / Target status
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long asnId, String status) {
        AsnRecord asn = asnRecordMapper.selectById(asnId);
        if (asn == null) {
            throw new BizException(6201, "ASN 记录不存在 / ASN record not found");
        }
        asn.setStatus(status);
        asnRecordMapper.updateById(asn);
        log.info("ASN 状态更新: asnNo={} status={}", asn.getAsnNo(), status);
    }

    /**
     * 采购方确认收货 / Buyer confirms receipt
     *
     * @param asnId ASN ID / ASN ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long asnId) {
        AsnRecord asn = asnRecordMapper.selectById(asnId);
        if (asn == null) {
            throw new BizException(6201, "ASN 记录不存在 / ASN record not found");
        }
        if (!STATUS_ARRIVED.equals(asn.getStatus()) && !STATUS_IN_TRANSIT.equals(asn.getStatus())) {
            throw new BizException(6202, "当前 ASN 状态不允许确认收货 / Current ASN status does not allow receipt confirmation");
        }
        asn.setStatus(STATUS_RECEIVED);
        asnRecordMapper.updateById(asn);
        log.info("ASN 已确认收货: asnNo={}", asn.getAsnNo());
    }

    /**
     * 按 ID 查询 ASN / Query ASN by ID
     */
    public AsnRecord getById(Long asnId) {
        AsnRecord asn = asnRecordMapper.selectById(asnId);
        if (asn == null) {
            throw new BizException(6201, "ASN 记录不存在 / ASN record not found");
        }
        return asn;
    }

    /**
     * 查询 ASN 明细列表 / Query ASN items
     */
    public List<AsnItem> listItems(Long asnId) {
        return asnItemMapper.selectList(
                new LambdaQueryWrapper<AsnItem>().eq(AsnItem::getAsnId, asnId));
    }

    /**
     * 分页查询 ASN（支持按订单/供应商/状态筛选） / Paginated query (supports order/supplier/status filter)
     */
    public Page<AsnRecord> page(Long orderId, Long supplierId, String status, int page, int size) {
        LambdaQueryWrapper<AsnRecord> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(AsnRecord::getOrderId, orderId);
        }
        if (supplierId != null) {
            wrapper.eq(AsnRecord::getSupplierId, supplierId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AsnRecord::getStatus, status);
        }
        wrapper.orderByDesc(AsnRecord::getCreatedAt);
        return asnRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * ASN 明细创建请求 / ASN item creation request
     */
    @lombok.Data
    public static class AsnItemRequest {
        private Long materialId;
        private String materialName;
        private java.math.BigDecimal quantity;
        private String unit;
        private String batchNo;
        private String packagingType;
        private Integer packageCount;
    }
}
