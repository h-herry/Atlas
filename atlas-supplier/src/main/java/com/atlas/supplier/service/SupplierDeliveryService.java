package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierDelivery;
import com.atlas.supplier.entity.SupplierDeliveryItem;
import com.atlas.supplier.feign.MessageFeignClient;
import com.atlas.supplier.mapper.SupplierDeliveryItemMapper;
import com.atlas.supplier.mapper.SupplierDeliveryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商发货协同 Service / Supplier delivery collaboration Service
 *
 * <p>供应商发货/物流跟踪/条码标签生成。 /
 * Supplier shipment / logistics tracking / barcode label generation.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierDeliveryService {

    private final SupplierDeliveryMapper deliveryMapper;
    private final SupplierDeliveryItemMapper deliveryItemMapper;
    private final MessageFeignClient messageFeignClient;

    /**
     * 创建发货单（如 PurchaseService 提交订单后联动） /
     * Create delivery order (linked after PurchaseService submits order)
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierDelivery create(SupplierDelivery delivery) {
        delivery.setStatus(0);
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setUpdatedAt(LocalDateTime.now());
        deliveryMapper.insert(delivery);
        log.info("发货单创建成功: deliveryNo={}", delivery.getDeliveryNo());
        return delivery;
    }

    /**
     * 供应商确认发货 / Supplier confirms shipment
     */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long deliveryId, String trackingNo, String carrier) {
        SupplierDelivery delivery = getById(deliveryId);
        if (delivery.getStatus() >= 1) {
            throw new BizException(12011, "发货单已发货或已收货，不可重复操作");
        }
        delivery.setStatus(1);
        delivery.setTrackingNo(trackingNo);
        delivery.setCarrier(carrier);
        delivery.setShippedAt(LocalDateTime.now());
        delivery.setUpdatedAt(LocalDateTime.now());
        deliveryMapper.updateById(delivery);

        // 实时消息推送: 发货通知 / Real-time message push: shipment notification
        pushDeliveryEvent(delivery, "supplier_delivery.shipped",
                "发货通知 / Shipment Notification",
                "订单 " + delivery.getDeliveryNo() + " 已发货，物流单号: " + trackingNo);

        log.info("供应商已发货: deliveryId={} trackingNo={}", deliveryId, trackingNo);
    }

    /**
     * 收货确认 / Confirm receipt
     */
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long deliveryId) {
        SupplierDelivery delivery = getById(deliveryId);
        if (delivery.getStatus() >= 3) {
            throw new BizException(ErrorCode.DELIVERY_ALREADY_RECEIVED);
        }
        delivery.setStatus(3);
        delivery.setUpdatedAt(LocalDateTime.now());
        deliveryMapper.updateById(delivery);

        // 实时消息推送: 到货确认 / Real-time message push: received confirmation
        pushDeliveryEvent(delivery, "supplier_delivery.received",
                "到货确认 / Goods Received Confirmation",
                "发货单 " + delivery.getDeliveryNo() + " 收货已确认，入库完成。");

        log.info("发货单收货确认: deliveryId={}", deliveryId);
    }

    /**
     * 添加发货明细 / Add delivery item
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierDeliveryItem addItem(SupplierDeliveryItem item) {
        item.setCreatedAt(LocalDateTime.now());
        deliveryItemMapper.insert(item);
        return item;
    }

    /**
     * 查询发货明细列表 / Query delivery item list
     */
    public List<SupplierDeliveryItem> listItems(Long deliveryId) {
        return deliveryItemMapper.selectList(
                new LambdaQueryWrapper<SupplierDeliveryItem>().eq(SupplierDeliveryItem::getDeliveryId, deliveryId));
    }

    /**
     * 查询发货单 / Query delivery order
     */
    public SupplierDelivery getById(Long id) {
        SupplierDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new BizException(ErrorCode.DELIVERY_NOT_EXIST);
        }
        return delivery;
    }

    /**
     * 分页查询发货单 / Paginated query of delivery orders
     */
    public Page<SupplierDelivery> page(String keyword, Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<SupplierDelivery> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SupplierDelivery::getDeliveryNo, keyword);
        }
        if (supplierId != null) {
            wrapper.eq(SupplierDelivery::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(SupplierDelivery::getStatus, status);
        }
        wrapper.orderByDesc(SupplierDelivery::getCreatedAt);
        return deliveryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 消息推送辅助方法 / Message push helper methods ====================

    /**
     * 异步推送交付事件消息（不阻塞主业务流程） /
     * Async push delivery event message (non-blocking to main business flow)
     */
    @Async
    protected void pushDeliveryEvent(SupplierDelivery delivery, String eventName, String title, String content) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("title", title);
            request.put("content", content);
            request.put("type", "DELIVERY");
            request.put("relatedId", delivery.getDeliveryNo());
            request.put("relatedType", "SUPPLIER_DELIVERY");
            request.put("supplierId", delivery.getSupplierId());
            messageFeignClient.pushToSupplier(request);
            log.info("Delivery 消息已推送: event={}, deliveryNo={}, supplierId={}",
                    eventName, delivery.getDeliveryNo(), delivery.getSupplierId());
        } catch (Exception e) {
            log.error("Delivery 消息推送失败(Feign调用异常,已降级): event={}, deliveryNo={}, error={}",
                    eventName, delivery.getDeliveryNo(), e.getMessage());
        }
    }

    /**
     * 定时检查交付延迟预警 / Scheduled delivery delay check and warning
     * <p>由 @Scheduled 定时任务触发，检查所有待发货且已超期的发货单。 /
     * Triggered by @Scheduled job, checks all pending deliveries past their estimated arrival date.</p>
     */
    public void checkAndWarnDelayed() {
        // 查询待发货(0)或部分收货(2)且预计到货日期已过的发货单 /
        // Query pending(0) or partial received(2) deliveries past estimated arrival
        LambdaQueryWrapper<SupplierDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SupplierDelivery::getStatus, 0, 2)
               .le(SupplierDelivery::getEstimatedArrival, LocalDateTime.now().toLocalDate());
        List<SupplierDelivery> delayedList = deliveryMapper.selectList(wrapper);

        for (SupplierDelivery delivery : delayedList) {
            pushDeliveryEvent(delivery, "supplier_delivery.delayed",
                    "交付延迟预警 / Delivery Delay Alert",
                    "发货单 " + delivery.getDeliveryNo()
                            + " 交付已延迟！预计到货日期 " + delivery.getEstimatedArrival()
                            + "，请立即采取措施。 / Delivery delayed! Estimated arrival: "
                            + delivery.getEstimatedArrival() + ". Take immediate action.");
        }
        if (!delayedList.isEmpty()) {
            log.info("Delivery 延迟预警扫描完成，共 {} 条 / delay scan complete, {} records",
                    delayedList.size(), delayedList.size());
        }
    }
}
