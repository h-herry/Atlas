package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.config.SupplierSecurityConfig;
import com.atlas.supplier.dto.portal.DeliveryUpdateRequest;
import com.atlas.supplier.entity.DeliveryOrder;
import com.atlas.supplier.entity.SupplierDelivery;
import com.atlas.supplier.mapper.DeliveryOrderMapper;
import com.atlas.supplier.mapper.SupplierDeliveryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流/交付服务（供应商端） — 供应商视角：发货管理、物流轨迹、延迟通知 /
 * Logistics/delivery service (portal) — supplier perspective: shipment management, tracking, delay notification
 *
 * <p>复用现有的 SupplierDelivery 和 DeliveryOrder 实体与 Mapper，通过 supplier_id 做数据隔离。 /
 * Reuses existing SupplierDelivery and DeliveryOrder entities and Mappers, with supplier_id data isolation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalDeliveryService {

    private final SupplierDeliveryMapper supplierDeliveryMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;

    /**
     * 发货任务列表（企业已下单待发货） / Delivery task list (enterprise orders pending shipment)
     *
     * @param page   页码 / Page number
     * @param size   每页条数 / Page size
     * @param status 状态筛选 / Status filter
     * @return 分页发货任务 / Paginated delivery tasks
     */
    public Page<SupplierDelivery> listDeliveries(int page, int size, Integer status) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        LambdaQueryWrapper<SupplierDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierDelivery::getSupplierId, supplierId);
        if (status != null) {
            wrapper.eq(SupplierDelivery::getStatus, status);
        }
        wrapper.orderByDesc(SupplierDelivery::getCreatedAt);

        return supplierDeliveryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 发货详情（收货地址、物料清单、要求到货日期） /
     * Delivery detail (receiving address, material list, required arrival date)
     *
     * @param deliveryId 发货单ID / Delivery ID
     * @return 发货详情 / Delivery detail
     */
    public SupplierDelivery getDeliveryDetail(Long deliveryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        SupplierDelivery delivery = supplierDeliveryMapper.selectById(deliveryId);
        if (delivery == null || !delivery.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权查看该发货单 / Not authorized to view this delivery order");
        }
        return delivery;
    }

    /**
     * 确认发货（录入物流公司、运单号、预计到达时间） /
     * Confirm shipment (enter logistics company, tracking number, estimated arrival)
     *
     * @param deliveryId 发货单ID / Delivery ID
     * @param request    发货信息 / Shipment info
     */
    public void confirmShipment(Long deliveryId, DeliveryUpdateRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 查询并验证发货单 / Query and validate delivery order
        SupplierDelivery delivery = supplierDeliveryMapper.selectById(deliveryId);
        if (delivery == null || !delivery.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该发货单 / Not authorized to operate this delivery");
        }

        // 状态校验：只有待发货状态可确认 / Status validation: only pending status can confirm shipment
        if (delivery.getStatus() != null && delivery.getStatus() != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "当前状态不允许确认发货，请刷新后重试 / Current status does not allow shipment confirmation, please refresh and retry");
        }

        // 更新发货信息 / Update delivery info
        delivery.setCarrier(request.getLogisticsCompany());
        delivery.setTrackingNo(request.getTrackingNo());
        delivery.setEstimatedArrival(request.getEstimatedArrival().toLocalDate());
        delivery.setShippedAt(LocalDateTime.now());
        delivery.setStatus(1); // 已发货 / Shipped

        supplierDeliveryMapper.updateById(delivery);

        // 同步更新 DeliveryOrder（若有） / Sync update DeliveryOrder (if exists)
        DeliveryOrder order = deliveryOrderMapper.selectOne(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getPurchaseOrderId, delivery.getPurchaseOrderId())
        );
        if (order != null) {
            order.setLogisticsCompany(request.getLogisticsCompany());
            order.setTrackingNo(request.getTrackingNo());
            order.setEstimatedArriveDate(request.getEstimatedArrival().toLocalDate());
            order.setStatus(1); // 运输中 / In transit
            deliveryOrderMapper.updateById(order);
        }

        log.info("供应商确认发货: deliveryId={}, supplierId={}, trackingNo={}",
                deliveryId, supplierId, request.getTrackingNo());
    }

    /**
     * 更新物流轨迹 / Update tracking trajectory
     *
     * @param deliveryId  发货单ID / Delivery ID
     * @param trackingInfo 物流轨迹信息 / Tracking info
     */
    public void updateTracking(Long deliveryId, Map<String, Object> trackingInfo) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        SupplierDelivery delivery = supplierDeliveryMapper.selectById(deliveryId);
        if (delivery == null || !delivery.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该发货单 / Not authorized to operate this delivery");
        }

        // 更新物流轨迹（可存入物流轨迹表或 Redis） /
        // Update tracking trajectory (can save to tracking table or Redis)
        log.info("更新物流轨迹: deliveryId={}, supplierId={}, info={}", deliveryId, supplierId, trackingInfo);
        // 实际实现可将轨迹记录写入 tracking_history 表 / Real implementation writes to tracking_history table
    }

    /**
     * 查看物流轨迹 / View tracking trajectory
     *
     * @param deliveryId 发货单ID / Delivery ID
     * @return 物流轨迹列表 / Tracking trajectory list
     */
    public List<Map<String, Object>> getTracking(Long deliveryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        SupplierDelivery delivery = supplierDeliveryMapper.selectById(deliveryId);
        if (delivery == null || !delivery.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权查看该物流信息 / Not authorized to view this tracking info");
        }

        log.info("查看物流轨迹: deliveryId={}, supplierId={}", deliveryId, supplierId);
        // return trackingMapper.selectByDeliveryId(deliveryId);
        return List.of();
    }

    /**
     * 历史发货记录 / Historical delivery records
     *
     * @param page 页码 / Page number
     * @param size 每页条数 / Page size
     * @return 分页历史记录 / Paginated history
     */
    public Page<SupplierDelivery> getDeliveryHistory(int page, int size) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        return supplierDeliveryMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<SupplierDelivery>()
                        .eq(SupplierDelivery::getSupplierId, supplierId)
                        .in(SupplierDelivery::getStatus, 2, 3) // 部分收货/已收货 / Partial received / Received
                        .orderByDesc(SupplierDelivery::getShippedAt)
        );
    }

    /**
     * 延迟通知（告知企业交期延迟原因） / Delay notification (inform enterprise of delivery delay reason)
     *
     * @param deliveryId 发货单ID / Delivery ID
     * @param reason     延迟原因 / Delay reason
     * @param newEstimate 新的预计到达时间 / New estimated arrival
     */
    public void notifyDelay(Long deliveryId, String reason, LocalDate newEstimate) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        SupplierDelivery delivery = supplierDeliveryMapper.selectById(deliveryId);
        if (delivery == null || !delivery.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该发货单 / Not authorized to operate this delivery");
        }

        // 更新预计到货日期 / Update estimated arrival date
        delivery.setEstimatedArrival(newEstimate);
        supplierDeliveryMapper.updateById(delivery);

        // 推送延迟通知给企业端 / Push delay notification to enterprise
        log.info("供应商提交延迟通知: deliveryId={}, supplierId={}, reason={}, newEstimate={}",
                deliveryId, supplierId, reason, newEstimate);
        // TODO: 调用消息推送服务通知企业端 / TODO: Call message push service to notify enterprise
    }
}
