package com.atlas.supplier.service;

import com.atlas.supplier.feign.MessageFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 供应商订单消息推送 Service — 订单创建/状态变更/交期预警的消息推送编排 /
 * Supplier order message push Service — orchestrates message push for order creation / status change / delivery warnings
 *
 * <p>被 PurchaseService 等业务模块调用，通过 Feign 推送实时消息到供应商端。 /
 * Called by PurchaseService and other business modules, pushes real-time messages to suppliers via Feign.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierOrderService {

    private final MessageFeignClient messageFeignClient;

    // ==================== 订单事件推送 / Order Event Push ====================

    /**
     * 订单生成后推送消息 / Push message after order creation
     * <p>事件: supplier_order.created /
     * Event: supplier_order.created</p>
     *
     * @param supplierId  供应商ID / Supplier ID
     * @param orderNo     订单编号 / Order number
     * @param orderAmount 订单金额 / Order amount
     */
    @Async
    public void onOrderCreated(Long supplierId, String orderNo, Object orderAmount) {
        String content = "您收到一笔新订单: " + orderNo + "，金额 ￥" + orderAmount
                + "，请及时确认并安排生产。 / You have received a new order: " + orderNo
                + ", amount ¥" + orderAmount + ", please confirm and arrange production promptly.";
        pushOrderEvent(supplierId, orderNo, "supplier_order.created",
                "新订单通知 / New Order Notification", content);
    }

    /**
     * 订单状态变更后推送消息 / Push message after order status changed
     * <p>事件: supplier_order.status_changed /
     * Event: supplier_order.status_changed</p>
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param orderNo    订单编号 / Order number
     * @param statusName 状态名称 / Status name
     */
    @Async
    public void onOrderStatusChanged(Long supplierId, String orderNo, String statusName) {
        String content = "订单 " + orderNo + " 状态已变更为【" + statusName + "】。"
                + "如有疑问请联系采购方。 / Order " + orderNo + " status has changed to ["
                + statusName + "]. Contact the purchaser if you have any questions.";
        pushOrderEvent(supplierId, orderNo, "supplier_order.status_changed",
                "订单状态变更 / Order Status Changed", content);
    }

    /**
     * 交期临近预警 / Delivery approaching warning
     * <p>由定时任务触发，检查交期 < 3 天的订单并推送预警 /
     * Triggered by scheduled job, checks orders with due date < 3 days and pushes warning</p>
     * <p>事件: supplier_order.delivery_approaching /
     * Event: supplier_order.delivery_approaching</p>
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param orderNo    订单编号 / Order number
     * @param dueDate    交期 / Due date
     */
    @Async
    public void onDeliveryApproaching(Long supplierId, String orderNo, LocalDate dueDate) {
        String content = "订单 " + orderNo + " 交期临近！预计交货日期: " + dueDate
                + "，请确保按时交付。 / Order " + orderNo + " delivery approaching! "
                + "Estimated delivery date: " + dueDate + ". Please ensure on-time delivery.";
        pushOrderEvent(supplierId, orderNo, "supplier_order.delivery_approaching",
                "交期临近预警 / Delivery Approaching Warning", content);
    }

    /**
     * 交期已过期预警推送 / Past-due order warning push
     * <p>由定时任务触发，检查已超过交期且仍未完成的订单 /
     * Triggered by scheduled job, checks orders past due date and still incomplete</p>
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param orderNo    订单编号 / Order number
     * @param dueDate    原定交期 / Original due date
     */
    @Async
    public void onDeliveryOverdue(Long supplierId, String orderNo, LocalDate dueDate) {
        String content = "订单 " + orderNo + " 已超过交期（" + dueDate + "）！"
                + "请立即处理并按最新计划交付。 / Order " + orderNo + " is past due ("
                + dueDate + ")! Please process immediately and deliver per latest schedule.";
        pushOrderEvent(supplierId, orderNo, "supplier_order.delivery_approaching",
                "交付超期预警 / Delivery Overdue Warning", content);
    }

    // ==================== 内部辅助方法 / Internal Helper ====================

    /**
     * 异步推送订单事件消息（不阻塞主业务流程） /
     * Async push order event message (non-blocking to main business flow)
     */
    private void pushOrderEvent(Long supplierId, String orderNo, String eventName,
                                String title, String content) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("title", title);
            request.put("content", content);
            request.put("type", "ORDER");
            request.put("relatedId", orderNo);
            request.put("relatedType", "PURCHASE_ORDER");
            request.put("supplierId", supplierId);
            messageFeignClient.pushToSupplier(request);
            log.info("Order 消息已推送: event={}, orderNo={}, supplierId={}",
                    eventName, orderNo, supplierId);
        } catch (Exception e) {
            log.error("Order 消息推送失败(Feign调用异常,已降级): event={}, orderNo={}, error={}",
                    eventName, orderNo, e.getMessage());
        }
    }
}
