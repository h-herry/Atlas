package com.atlas.common.message.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 消息推送事件 — 由业务 Service 发布，触发消息持久化 + WebSocket 推送 /
 * Message push event — published by business services, triggers message persistence + WebSocket push
 *
 * <p>典型用法 (在业务 Service 中): /
 * Typical usage (in business Service):
 * <pre>{@code
 * @Autowired private ApplicationEventPublisher publisher;
 *
 * publisher.publishEvent(MessagePushEvent.builder()
 *     .title("订单状态变更通知")
 *     .content("订单 PO-2026-0001 状态已变更为【已发货】")
 *     .type(MessagePushEvent.TYPE_ORDER)
 *     .relatedId("PO-2026-0001")
 *     .supplierId(1001L)
 *     .build());
 * }</pre>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
public class MessagePushEvent extends ApplicationEvent {

    // ========== 消息类型常量 / Message type constants ==========

    /** 订单相关 / Order related */
    public static final String TYPE_ORDER = "ORDER";
    /** 交付相关 / Delivery related */
    public static final String TYPE_DELIVERY = "DELIVERY";
    /** 结算相关 / Settlement related */
    public static final String TYPE_SETTLEMENT = "SETTLEMENT";
    /** 系统通知 / System notification */
    public static final String TYPE_SYSTEM = "SYSTEM";
    /** 审批相关 / Approval related */
    public static final String TYPE_APPROVAL = "APPROVAL";
    /** 质量相关 / Quality related */
    public static final String TYPE_QUALITY = "QUALITY";

    // ========== 事件子类型常量 / Event sub-type constants ==========

    /** 订单已创建 / Order created */
    public static final String EVENT_ORDER_CREATED = "supplier_order.created";
    /** 订单状态变更 / Order status changed */
    public static final String EVENT_ORDER_STATUS_CHANGED = "supplier_order.status_changed";
    /** 交期临近预警 / Delivery approaching warning */
    public static final String EVENT_DELIVERY_APPROACHING = "supplier_order.delivery_approaching";
    /** 已发货 / Shipped */
    public static final String EVENT_DELIVERY_SHIPPED = "supplier_delivery.shipped";
    /** 已到货 / Received */
    public static final String EVENT_DELIVERY_RECEIVED = "supplier_delivery.received";
    /** 延迟预警 / Delayed warning */
    public static final String EVENT_DELIVERY_DELAYED = "supplier_delivery.delayed";
    /** 对账单已生成 / Statement generated */
    public static final String EVENT_SETTLEMENT_STATEMENT = "supplier_settlement.statement_generated";
    /** 付款状态变更 / Payment updated */
    public static final String EVENT_SETTLEMENT_PAYMENT = "supplier_settlement.payment_updated";

    // ========== 事件字段 / Event fields ==========

    private final String title;
    private final String content;
    private final String type;
    private final String relatedId;
    private final String relatedType;
    private final Long supplierId;
    private final Long userId;
    private final String eventName;

    @Builder
    public MessagePushEvent(Object source, String title, String content, String type,
                            String relatedId, String relatedType, Long supplierId,
                            Long userId, String eventName) {
        super(source != null ? source : new Object());
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.supplierId = supplierId;
        this.userId = userId;
        this.eventName = eventName;
    }
}
