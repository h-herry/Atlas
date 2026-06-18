package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.DeliveryOrder;
import com.atlas.supplier.entity.ForecastNotice;
import com.atlas.supplier.entity.Reconciliation;
import com.atlas.supplier.mapper.DeliveryOrderMapper;
import com.atlas.supplier.mapper.ForecastNoticeMapper;
import com.atlas.supplier.mapper.ReconciliationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商协同管理 Service — 发布预测 → 供应商查看 → 发货 → 物流追踪 → 到货确认 → 生成对账单 → 双方确认 → 推送开票通知 /
 * Supplier collaboration Service — publish forecast → supplier view → delivery → logistics tracking → arrival confirm → create reconciliation → both-party confirm → push invoice notice
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierCollaborationService {

    private final ForecastNoticeMapper forecastNoticeMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final ReconciliationMapper reconciliationMapper;

    // ==================== 预测计划 / Forecast ====================

    /**
     * 发布预测计划 / Publish forecast plan
     */
    @Transactional(rollbackFor = Exception.class)
    public ForecastNotice publishForecast(ForecastNotice forecast) {
        forecastNoticeMapper.insert(forecast);
        log.info("预测计划已发布: supplierId={}, period={}", forecast.getSupplierId(), forecast.getForecastPeriod());
        return forecast;
    }

    /**
     * 分页查询预测计划（供应商视角查看） / Paginated forecast query (supplier view)
     */
    public Page<ForecastNotice> pageForecast(Long supplierId, int page, int size) {
        LambdaQueryWrapper<ForecastNotice> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(ForecastNotice::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(ForecastNotice::getCreatedAt);
        return forecastNoticeMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 发货单 / Delivery Order ====================

    /**
     * 创建发货单（供应商发货） / Create delivery order (supplier shipment)
     */
    @Transactional(rollbackFor = Exception.class)
    public DeliveryOrder createDelivery(DeliveryOrder delivery) {
        delivery.setStatus(0); // 待发货 / Pending
        deliveryOrderMapper.insert(delivery);
        log.info("发货单已创建: deliveryNo={}", delivery.getDeliveryNo());
        return delivery;
    }

    /**
     * 物流追踪 — 更新运输状态 / Logistics tracking — update transport status
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLogistics(Long deliveryId, String logisticsCompany, String trackingNo, Integer status) {
        DeliveryOrder delivery = deliveryOrderMapper.selectById(deliveryId);
        if (delivery == null) {
            throw new BizException(ErrorCode.DELIVERY_ORDER_NOT_EXIST);
        }
        delivery.setLogisticsCompany(logisticsCompany);
        delivery.setTrackingNo(trackingNo);
        delivery.setStatus(status);
        deliveryOrderMapper.updateById(delivery);
        log.info("物流状态已更新: deliveryId={}, status={}", deliveryId, status);
    }

    /**
     * 到货确认 / Confirm arrival
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmArrival(Long deliveryId) {
        DeliveryOrder delivery = deliveryOrderMapper.selectById(deliveryId);
        if (delivery == null) {
            throw new BizException(ErrorCode.DELIVERY_ORDER_NOT_EXIST);
        }
        delivery.setStatus(3); // 已签收 / Signed
        delivery.setActualArriveDate(LocalDate.now());
        deliveryOrderMapper.updateById(delivery);
        log.info("收货已确认: deliveryId={}", deliveryId);
    }

    /**
     * 分页查询发货单 / Paginated delivery order query
     */
    public Page<DeliveryOrder> pageDelivery(Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(DeliveryOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(DeliveryOrder::getStatus, status);
        }
        wrapper.orderByDesc(DeliveryOrder::getCreatedAt);
        return deliveryOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 对账单 / Reconciliation ====================

    /**
     * 生成对账单 / Create reconciliation
     */
    @Transactional(rollbackFor = Exception.class)
    public Reconciliation createReconciliation(Reconciliation reconciliation) {
        // 自动计算应付净额 = 采购总额 - 退货总额 / Auto-calc net amount = purchase total - return total
        BigDecimal netAmount = reconciliation.getPurchaseTotal()
                .subtract(reconciliation.getReturnTotal());
        reconciliation.setNetAmount(netAmount);
        reconciliation.setStatus(0); // 待确认 / Pending
        reconciliationMapper.insert(reconciliation);
        log.info("对账单已生成: reconcilNo={}, netAmount={}", reconciliation.getReconcilNo(), netAmount);
        return reconciliation;
    }

    /**
     * 供应商确认对账单 / Supplier confirms reconciliation
     */
    @Transactional(rollbackFor = Exception.class)
    public void supplierConfirm(Long reconciliationId, String confirmedBy) {
        Reconciliation reconciliation = reconciliationMapper.selectById(reconciliationId);
        if (reconciliation == null) {
            throw new BizException(ErrorCode.RECONCILIATION_NOT_EXIST);
        }
        reconciliation.setStatus(1); // 供应商确认 / Supplier confirmed
        reconciliation.setConfirmedBy(confirmedBy);
        reconciliation.setConfirmedAt(LocalDateTime.now());
        reconciliationMapper.updateById(reconciliation);
        log.info("供应商已确认对账单: reconcilId={}", reconciliationId);
    }

    /**
     * 采购方确认对账单 / Purchaser confirms reconciliation
     */
    @Transactional(rollbackFor = Exception.class)
    public void purchaserConfirm(Long reconciliationId, String confirmedBy) {
        Reconciliation reconciliation = reconciliationMapper.selectById(reconciliationId);
        if (reconciliation == null) {
            throw new BizException(ErrorCode.RECONCILIATION_NOT_EXIST);
        }
        if (reconciliation.getStatus() != 1) {
            throw new BizException(ErrorCode.RECONCILIATION_ALREADY_CONFIRMED);
        }
        reconciliation.setStatus(2); // 采购方确认 / Purchaser confirmed
        reconciliation.setConfirmedBy(confirmedBy);
        reconciliation.setConfirmedAt(LocalDateTime.now());
        reconciliationMapper.updateById(reconciliation);
        log.info("采购方已确认对账单: reconcilId={}", reconciliationId);
    }

    /**
     * 开票通知（状态流转至已开票） / Mark as invoiced (status → invoiced)
     */
    @Transactional(rollbackFor = Exception.class)
    public void markInvoiced(Long reconciliationId) {
        Reconciliation reconciliation = reconciliationMapper.selectById(reconciliationId);
        if (reconciliation == null) {
            throw new BizException(ErrorCode.RECONCILIATION_NOT_EXIST);
        }
        reconciliation.setStatus(3); // 已开票 / Invoiced
        reconciliationMapper.updateById(reconciliation);
        log.info("对账单已开票: reconcilId={}", reconciliationId);
    }

    /**
     * 分页查询对账单 / Paginated reconciliation query
     */
    public Page<Reconciliation> pageReconciliation(Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<Reconciliation> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(Reconciliation::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(Reconciliation::getStatus, status);
        }
        wrapper.orderByDesc(Reconciliation::getCreatedAt);
        return reconciliationMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
