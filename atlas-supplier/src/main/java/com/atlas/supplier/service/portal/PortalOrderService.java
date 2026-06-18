package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.config.SupplierSecurityConfig;
import com.atlas.supplier.dto.portal.DelayReportRequest;
import com.atlas.supplier.dto.portal.DeliveryCommitmentRequest;
import com.atlas.supplier.dto.portal.OrderConfirmRequest;
import com.atlas.supplier.dto.portal.OrderDetailRequest;
import com.atlas.supplier.dto.portal.OrderFulfillmentRequest;
import com.atlas.supplier.dto.portal.ProductionProgressRequest;
import com.atlas.supplier.entity.DeliveryOrder;
import com.atlas.supplier.mapper.DeliveryOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务（供应商端） — 供应商视角：采购订单确认、履行状态、工作台概览 /
 * Order service (portal) — supplier perspective: purchase order confirmation, fulfillment status, dashboard overview
 *
 * <p>复用现有的 DeliveryOrder 实体和 Mapper（代表采购订单），通过 supplier_id 做数据隔离。 /
 * Reuses existing DeliveryOrder entity and Mapper (represents purchase orders), with supplier_id data isolation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalOrderService {

    private final DeliveryOrderMapper deliveryOrderMapper;

    /**
     * 采购订单列表（企业下达的订单，分页、状态筛选） /
     * Purchase order list (orders placed by enterprises, paginated, status filter)
     *
     * @param page   页码 / Page number
     * @param size   每页条数 / Page size
     * @param status 状态筛选（可选: 0待确认 1已确认 2生产中 3已发货 4已完成 5已取消）/ Status filter (optional)
     * @return 分页订单 / Paginated orders
     */
    public Page<DeliveryOrder> listOrders(int page, int size, Integer status) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryOrder::getSupplierId, supplierId);
        if (status != null) {
            wrapper.eq(DeliveryOrder::getStatus, status);
        }
        wrapper.orderByDesc(DeliveryOrder::getCreatedAt);

        return deliveryOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 订单详情（物料、数量、单价、交期、收货信息） /
     * Order detail (material, quantity, unit price, delivery date, receiving info)
     *
     * @param orderId 订单ID / Order ID
     * @return 订单详情 / Order detail
     */
    public DeliveryOrder getOrderDetail(Long orderId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权查看该订单 / Not authorized to view this order");
        }
        return order;
    }

    /**
     * 确认接单 / Confirm order acceptance
     *
     * @param orderId 订单ID / Order ID
     * @param request 确认请求 / Confirm request
     */
    public void confirmOrder(Long orderId, OrderConfirmRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 状态校验：只有待确认状态可接单 / Status validation: only pending status can be confirmed
        if (order.getStatus() != null && order.getStatus() != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "当前订单状态不允许接单 / Current order status does not allow confirmation");
        }

        order.setStatus(1); // 已确认 / Confirmed
        deliveryOrderMapper.updateById(order);

        log.info("供应商确认接单: orderId={}, supplierId={}", orderId, supplierId);
    }

    /**
     * 拒绝接单（含原因） / Reject order (with reason)
     *
     * @param orderId 订单ID / Order ID
     * @param request 拒绝请求 / Reject request
     */
    public void rejectOrder(Long orderId, OrderConfirmRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 状态校验 / Status validation
        if (order.getStatus() != null && order.getStatus() != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "当前订单状态不允许拒绝 / Current order status does not allow rejection");
        }

        order.setStatus(5); // 已取消 / Cancelled
        deliveryOrderMapper.updateById(order);

        log.info("供应商拒绝接单: orderId={}, supplierId={}, reason={}",
                orderId, supplierId, request.getRejectReason());
    }

    /**
     * 更新履行状态（生产中/已部分发货/已全部发货） /
     * Update fulfillment status (producing / partially shipped / fully shipped)
     *
     * @param orderId 订单ID / Order ID
     * @param request 履行更新 / Fulfillment update
     */
    public void updateFulfillment(Long orderId, OrderFulfillmentRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 状态映射: PRODUCING→2, PARTIAL_SHIPPED→3, FULL_SHIPPED→4 /
        // Status mapping: PRODUCING→2, PARTIAL_SHIPPED→3, FULL_SHIPPED→4
        int mappedStatus = mapFulfillmentStatus(request.getFulfillmentStatus());
        order.setStatus(mappedStatus);
        deliveryOrderMapper.updateById(order);

        log.info("供应商更新订单履行状态: orderId={}, supplierId={}, status={}->{}",
                orderId, supplierId, request.getFulfillmentStatus(), mappedStatus);
    }

    /**
     * 订单统计（待确认数、履行中数、已完成数、逾期数） /
     * Order statistics (pending confirm, in progress, completed, overdue)
     *
     * @return 统计数据 / Statistics
     */
    public Map<String, Object> getOrderStatistics() {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 待确认 / Pending confirmation
        Long pendingCount = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .eq(DeliveryOrder::getStatus, 0)
        );

        // 履行中（已确认 + 运输中） / In progress (confirmed + in transit)
        Long inProgressCount = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .in(DeliveryOrder::getStatus, 1, 2)
        );

        // 已完成（已签收） / Completed (signed)
        Long completedCount = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .eq(DeliveryOrder::getStatus, 3)
        );

        // 逾期（预计到达日期 < 当前日期 且 未签收） / Overdue (estimated arrival < now and not signed)
        Long overdueCount = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .lt(DeliveryOrder::getEstimatedArriveDate, LocalDateTime.now().toLocalDate())
                        .notIn(DeliveryOrder::getStatus, 3, 5)
        );

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingConfirm", pendingCount);
        stats.put("inProgress", inProgressCount);
        stats.put("completed", completedCount);
        stats.put("overdue", overdueCount);
        return stats;
    }

    /**
     * 供应商工作台概览（今日待办：待确认订单/待发货/待签合同/待报价） /
     * Supplier dashboard overview (today's todos: pending orders / pending shipments / pending contracts / pending quotes)
     *
     * @return 工作台概览 / Dashboard overview
     */
    public Map<String, Object> getDashboard() {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 待确认订单 / Pending order confirmation
        Long pendingOrders = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .eq(DeliveryOrder::getStatus, 0)
        );

        // 待发货 / Pending shipment
        Long pendingShipments = deliveryOrderMapper.selectCount(
                new LambdaQueryWrapper<DeliveryOrder>()
                        .eq(DeliveryOrder::getSupplierId, supplierId)
                        .eq(DeliveryOrder::getStatus, 1)
        );

        // TODO: 待签合同 (需集成 atlas-contract) / Pending contracts (requires atlas-contract integration)
        // TODO: 待报价 (需集成 atlas-purchase) / Pending quotes (requires atlas-purchase integration)

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingOrders", pendingOrders);
        dashboard.put("pendingShipments", pendingShipments);
        dashboard.put("pendingContracts", 0);  // 待集成 / Pending integration
        dashboard.put("pendingQuotes", 0);     // 待集成 / Pending integration
        dashboard.put("supplierId", supplierId);
        dashboard.put("updateTime", LocalDateTime.now());
        return dashboard;
    }

    // ==================== 订单明细与进度管理 / Order Details & Progress Management ====================

    /**
     * 填写订单详细信息 — 供应商收到采购单据后补充物料规格、单价、批次等细节 /
     * Fill order details — supplier supplements material spec, unit price, batch etc. after receiving purchase document
     *
     * @param orderId 订单ID / Order ID
     * @param request 订单明细请求 / Order detail request
     */
    @Transactional
    public void fillOrderDetails(Long orderId, OrderDetailRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 仅待确认和已确认状态的订单可补充明细 / Only pending and confirmed orders can fill details
        if (order.getStatus() != null && order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "当前订单状态不允许填写明细 / Current order status does not allow filling details");
        }

        // 将订单明细存储为 JSON 到 deliveryItems 字段（含物料规格、单价、批次等） /
        // Store order details as JSON into deliveryItems field (material spec, unit price, batch etc.)
        order.setDeliveryItems(buildOrderDetailJson(request));
        deliveryOrderMapper.updateById(order);

        log.info("供应商填写订单明细: orderId={}, supplierId={}, materialSpec={}, unitPrice={}",
                orderId, supplierId, request.getMaterialSpec(), request.getUnitPrice());
    }

    /**
     * 更新生产进度 — 供应商上报当前生产完成百分比、所处阶段、质检状态 /
     * Update production progress — supplier reports completion percentage, current stage, QC status
     *
     * @param orderId 订单ID / Order ID
     * @param request 生产进度请求 / Production progress request
     */
    @Transactional
    public void updateProductionProgress(Long orderId, ProductionProgressRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 仅已确认状态的订单可更新进度 / Only confirmed orders can update progress
        if (order.getStatus() == null || order.getStatus() < 1) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "仅已确认的订单可更新生产进度 / Only confirmed orders can update production progress");
        }
        if (order.getStatus() == 5) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "已取消的订单不可更新进度 / Cancelled orders cannot update progress");
        }

        // 根据进度自动映射状态: 0%→生产中(2), >0%且<100%→生产中(2), 100%→已全部发货(4) /
        // Auto-map status by progress: 0%→producing(2), >0%&<100%→producing(2), 100%→fully shipped(4)
        if (request.getProgressPercent() != null && request.getProgressPercent() >= 100) {
            order.setStatus(4); // 已全部发货 / Fully shipped
        } else if (order.getStatus() == 1) {
            order.setStatus(2); // 进入生产 / Enter production
        }

        deliveryOrderMapper.updateById(order);

        log.info("供应商更新生产进度: orderId={}, supplierId={}, progress={}%, stage={}, qcPassed={}",
                orderId, supplierId, request.getProgressPercent(),
                request.getCurrentStage(), request.getQualityCheckPassed());
    }

    /**
     * 承诺交期 — 供应商确认并承诺具体交付日期及生产计划 /
     * Commit delivery date — supplier confirms and commits specific delivery date with production plan
     *
     * @param orderId 订单ID / Order ID
     * @param request 交期承诺请求 / Delivery commitment request
     */
    @Transactional
    public void commitDeliveryDate(Long orderId, DeliveryCommitmentRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 已取消的订单不可承诺交期 / Cancelled orders cannot commit delivery
        if (order.getStatus() != null && order.getStatus() == 5) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "已取消的订单不可承诺交期 / Cancelled orders cannot commit delivery date");
        }

        // 承诺交期必须晚于当前日期 / Committed date must be after today
        if (request.getCommittedDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "承诺交期不能早于当前日期 / Committed delivery date must not be earlier than today");
        }

        order.setEstimatedArriveDate(request.getCommittedDate());
        deliveryOrderMapper.updateById(order);

        log.info("供应商承诺交期: orderId={}, supplierId={}, committedDate={}, plan={}",
                orderId, supplierId, request.getCommittedDate(), request.getProductionPlan());
    }

    /**
     * 交期延迟报备 — 供应商因故无法按期交付时报备原因及新预计交期 /
     * Report delivery delay — supplier reports delay reason and new estimated delivery date when unable to deliver on time
     *
     * @param orderId 订单ID / Order ID
     * @param request 延迟报备请求 / Delay report request
     */
    @Transactional
    public void reportDelay(Long orderId, DelayReportRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权操作该订单 / Not authorized to operate this order");
        }

        // 已取消或已完成的订单不可延迟报备 / Cancelled or completed orders cannot report delay
        if (order.getStatus() != null && (order.getStatus() == 5 || order.getStatus() == 3)) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "当前订单状态不允许延迟报备 / Current order status does not allow delay reporting");
        }

        // 新预计交期必须晚于当前日期 / New estimated date must be after today
        if (request.getNewEstimatedDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "新预计交期不能早于当前日期 / New estimated delivery date must not be earlier than today");
        }

        // 更新预计到达日期为新交期 / Update estimated arrival date to new delivery date
        order.setEstimatedArriveDate(request.getNewEstimatedDate());
        deliveryOrderMapper.updateById(order);

        log.warn("供应商延迟报备: orderId={}, supplierId={}, reason={}, newDate={}, impact={}",
                orderId, supplierId, request.getDelayReason(),
                request.getNewEstimatedDate(), request.getImpactDescription());
    }

    /**
     * 获取订单履行时间线 — 展示订单从下达到完成的完整状态变化轨迹 /
     * Get order fulfillment timeline — display complete status change trajectory from order placement to completion
     *
     * @param orderId 订单ID / Order ID
     * @return 时间线列表 / Timeline list
     */
    public List<Map<String, Object>> getOrderFulfillmentTimeline(Long orderId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
        if (order == null || !order.getSupplierId().equals(supplierId)) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "无权查看该订单 / Not authorized to view this order");
        }

        // 根据订单当前状态构建时间线 / Build timeline based on current order status
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 节点1: 订单下达 / Node 1: Order placed
        timeline.add(buildTimelineNode("order_placed", "订单下达", "Order Placed",
                order.getCreatedAt(), "企业已下达采购订单 / Enterprise has placed purchase order"));

        // 节点2: 确认接单（status >= 1） / Node 2: Confirmed (status >= 1)
        if (order.getStatus() != null && order.getStatus() >= 1) {
            timeline.add(buildTimelineNode("confirmed", "确认接单", "Confirmed",
                    order.getUpdatedAt(), "供应商已确认接单 / Supplier has confirmed the order"));
        }

        // 节点3: 生产中（status >= 2） / Node 3: Producing (status >= 2)
        if (order.getStatus() != null && order.getStatus() >= 2) {
            timeline.add(buildTimelineNode("producing", "生产中", "In Production",
                    order.getUpdatedAt(), "订单进入生产阶段 / Order entered production stage"));
        }

        // 节点4: 已发货（status >= 3） / Node 4: Shipped (status >= 3)
        if (order.getStatus() != null && order.getStatus() >= 3 && order.getTrackingNo() != null) {
            timeline.add(buildTimelineNode("shipped", "已发货",
                    "Shipped (" + order.getLogisticsCompany() + ": " + order.getTrackingNo() + ")",
                    order.getUpdatedAt(), "物流已发出 / Shipment dispatched"));
        }

        // 节点5: 已完成（status >= 4 且 status != 5） / Node 5: Completed (status >= 4 and status != 5)
        if (order.getStatus() != null && order.getStatus() == 4 && order.getStatus() != 5) {
            timeline.add(buildTimelineNode("completed", "已完成", "Completed",
                    order.getUpdatedAt(), "订单已完成交付 / Order delivery completed"));
        }

        // 节点6: 已取消（status == 5） / Node 6: Cancelled (status == 5)
        if (order.getStatus() != null && order.getStatus() == 5) {
            timeline.add(buildTimelineNode("cancelled", "已取消", "Cancelled",
                    order.getUpdatedAt(), "订单已取消 / Order has been cancelled"));
        }

        return timeline;
    }

    // ==================== 内部辅助方法 / Internal Helper ====================

    /**
     * 履行状态映射 / Fulfillment status mapping
     */
    private int mapFulfillmentStatus(String status) {
        return switch (status.toUpperCase()) {
            case "PRODUCING" -> 2;        // 生产中 / Producing
            case "PARTIAL_SHIPPED" -> 3;  // 已部分发货 / Partially shipped
            case "FULL_SHIPPED" -> 4;     // 已全部发货 / Fully shipped
            default -> throw new BizException(ErrorCode.BAD_REQUEST,
                    "无效的履行状态: " + status + " / Invalid fulfillment status");
        };
    }

    /**
     * 构建订单明细 JSON 字符串 / Build order detail JSON string
     */
    private String buildOrderDetailJson(OrderDetailRequest request) {
        return String.format(
                "{\"materialSpec\":\"%s\",\"unitPrice\":%s,\"estimatedDeliveryDate\":\"%s\"," +
                "\"productionBatch\":\"%s\",\"minOrderQuantity\":%d,\"leadTimeDays\":%d," +
                "\"packagingType\":\"%s\",\"remark\":\"%s\"}",
                escapeJson(request.getMaterialSpec()),
                request.getUnitPrice(),
                request.getEstimatedDeliveryDate(),
                escapeJson(request.getProductionBatch()),
                request.getMinOrderQuantity() != null ? request.getMinOrderQuantity() : 0,
                request.getLeadTimeDays() != null ? request.getLeadTimeDays() : 0,
                escapeJson(request.getPackagingType()),
                escapeJson(request.getRemark())
        );
    }

    /**
     * 构建时间线节点 / Build timeline node
     */
    private Map<String, Object> buildTimelineNode(String code, String labelCn, String labelEn,
                                                   LocalDateTime timestamp, String description) {
        Map<String, Object> node = new HashMap<>();
        node.put("code", code);                  // 节点代码 / Node code
        node.put("label", labelCn + " / " + labelEn);  // 节点标签（双语） / Node label (bilingual)
        node.put("timestamp", timestamp);         // 时间戳 / Timestamp
        node.put("description", description);     // 描述 / Description
        return node;
    }

    /**
     * JSON 字符串转义 / JSON string escape
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
