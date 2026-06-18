package com.atlas.purchase.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.*;
import com.atlas.purchase.enums.ProcurementTypeEnum;
import com.atlas.purchase.mapper.PurchaseOrderItemMapper;
import com.atlas.purchase.mapper.PurchaseOrderMapper;
import com.atlas.purchase.model.PurchaseCreateRequest;
import com.atlas.purchase.model.PurchaseCreateRequest.OrderItemRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购订单核心业务服务 — 负责采购单的创建、提交（含幂等校验与库存扣减自旋重试）、分页查询及采购方式单据联动
 * Purchase order core business service — handles order creation, submission (with idempotency check and
 * inventory deduction spin-retry), paginated query, and procurement document creation linkage
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderItemMapper itemMapper;
    private final NegotiationService negotiationService;
    private final ConsultationService consultationService;
    private final FrameworkService frameworkService;
    private final OpenBiddingService openBiddingService;
    private final InvitedBiddingService invitedBiddingService;
    private final InquiryService inquiryService;
    private final AuctionService auctionService;
    private final SingleSourceService singleSourceService;
    private final CooperativeInnovationService cooperativeInnovationService;
    private final RestTemplate restTemplate;

    /** 库存扣减最大重试次数（乐观锁自旋） / Max retry count for inventory deduction (optimistic-lock spin) */
    private static final int MAX_RETRY = 3;
    /** 自旋重试基础等待时间（ms），指数退避：100 → 200 → 400 / Spin-retry base wait time (ms), exponential backoff: 100 → 200 → 400 */
    private static final long RETRY_BASE_DELAY_MS = 100;

    /** 订单状态常量 / Order status constants */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PENDING_APPROVAL = 1;
    public static final int STATUS_SUBMITTED = 1; // 提交后等同于待审批 / After submission, equivalent to pending approval
    public static final int STATUS_APPROVED = 2;
    public static final int STATUS_EXECUTING = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELLED = 5;

    /**
     * 创建采购单（草稿状态）及其明细 — 生成订单编号、计算总金额、持久化主表与明细
     * Create a purchase order (draft status) with its line items — generate order number, calculate total amount,
     * persist header and item records
     *
     * @param request 采购创建请求 / purchase creation request
     * @return 创建成功的采购订单 / the created purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createOrder(PurchaseCreateRequest request) {
        // 1. 生成订单编号 / 1. Generate order number
        String orderNo = generateOrderNo();

        // 2. 计算总金额 / 2. Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal quantity = itemReq.getQuantity();
            BigDecimal unitPrice = itemReq.getUnitPrice();
            BigDecimal itemTotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setSkuId(itemReq.getSkuId());
            item.setSkuName(itemReq.getSkuName());
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(itemTotal);
            items.add(item);

            totalAmount = totalAmount.add(itemTotal);
        }

        // 3. 插入主表 / 3. Insert order header
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setContractId(request.getContractId());
        order.setSupplierId(request.getSupplierId());
        order.setDeptId(request.getDeptId());
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(STATUS_DRAFT);
        order.setProcurementType(request.getProcurementType());
        order.setRequestId(request.getRequestId());
        order.setCreatedBy(request.getCreatedBy());
        orderMapper.insert(order);

        // 4. 插入明细表 / 4. Insert order items
        for (PurchaseOrderItem item : items) {
            item.setOrderId(order.getId());
            itemMapper.insert(item);
        }

        log.info("创建采购单成功: orderNo={} totalAmount={} itemCount={}", orderNo, totalAmount, items.size());
        return order;
    }

    /**
     * 提交采购订单 — 幂等校验 → 状态校验 → 乐观锁自旋扣库存 → 更新状态 → 联动采购方式单据
     * Submit purchase order — idempotency check → status validation → optimistic-lock spin-retry deduction →
     * status update → procurement document linkage
     * <p>
     * 流程 / Flow:
     * <ol>
     *   <li>幂等校验：若 requestId 已存在则直接返回已有订单 / Idempotency check: return existing order if requestId already submitted</li>
     *   <li>校验订单状态必须为草稿 / Validate order status must be DRAFT</li>
     *   <li>乐观锁自旋扣减库存（最多3次，指数退避） / Optimistic-lock spin-retry deduction (max 3 times, exponential backoff)</li>
     *   <li>扣减成功后更新订单状态为已提交 / On success, update order status to SUBMITTED</li>
     * </ol>
     *
     * @param orderId 采购订单ID / purchase order ID
     * @return 提交后的订单 / the submitted order
     */
    @GlobalTransactional(timeoutMills = 300000)
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder submitOrder(Long orderId) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST);
        }

        // 幂等校验：若 requestId 已有提交记录则直接返回 / Idempotency check: return existing order if requestId already submitted
        if (order.getRequestId() != null && orderMapper.countByRequestId(order.getRequestId()) > 1) {
            log.info("幂等命中: requestId={} 已存在提交订单", order.getRequestId());
            return order;
        }

        // 状态校验：仅草稿状态可提交 / Status check: only DRAFT status can be submitted
        if (order.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY);
        }

        // 查询明细 / Query order items
        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, orderId));

        // 乐观锁自旋扣减库存 / Optimistic-lock spin-retry inventory deduction
        boolean deducted = deductInventoryWithRetry(order.getOrderNo(), items);

        if (!deducted) {
            log.error("库存扣减失败: orderNo={} 已重试{}次", order.getOrderNo(), MAX_RETRY);
            // 回滚：将订单状态置为已取消 / Rollback: set order status to cancelled
            order.setStatus(STATUS_CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
            throw new BizException(ErrorCode.STOCK_INSUFFICIENT, "库存扣减失败，已重试" + MAX_RETRY + "次");
        }

        // 更新订单状态 / Update order status
        order.setStatus(STATUS_SUBMITTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 根据采购方式自动创建对应的采购方式单据 / Auto-create corresponding procurement document by procurement type
        createProcurementDocument(order);

        log.info("采购单提交成功: orderNo={} status=SUBMITTED procurementType={}",
                order.getOrderNo(), order.getProcurementType());
        return order;
    }

    /**
     * 根据采购方式自动创建对应的采购方式单据（竞争性谈判/磋商/框架协议/公开招标/邀请招标/询比/竞价/单一来源/合作创新）
     * Auto-create the corresponding procurement document based on procurement type
     * (negotiation / consultation / framework / open-bidding / invited-bidding / inquiry / auction / single-source / collaborative)
     *
     * @param order 已提交的采购订单 / the submitted purchase order
     */
    private void createProcurementDocument(PurchaseOrder order) {
        Integer procurementType = order.getProcurementType();
        if (procurementType == null) {
            return;
        }
        ProcurementTypeEnum typeEnum = ProcurementTypeEnum.fromCode(procurementType);

        if (typeEnum == ProcurementTypeEnum.NEGOTIATION) {
            NegotiationSession session = new NegotiationSession();
            session.setPurchaseOrderId(order.getId());
            session.setStatus(NegotiationService.STATUS_DRAFT);
            negotiationService.createSession(session);
            log.info("已联动创建竞争性谈判单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.CONSULTATION) {
            ConsultationSession session = new ConsultationSession();
            session.setPurchaseOrderId(order.getId());
            session.setStatus(ConsultationService.STATUS_DRAFT);
            consultationService.createSession(session);
            log.info("已联动创建竞争性磋商单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.FRAMEWORK) {
            FrameworkAgreement agreement = new FrameworkAgreement();
            agreement.setStatus(FrameworkService.STATUS_DRAFT);
            frameworkService.createAgreement(agreement);
            log.info("已联动创建框架协议单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.OPEN_BIDDING) {
            openBiddingService.createFromOrder(order);
            log.info("已联动创建公开招标单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.INVITED_BIDDING) {
            invitedBiddingService.createFromOrder(order);
            log.info("已联动创建邀请招标单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.INQUIRY) {
            inquiryService.createFromOrder(order);
            log.info("已联动创建询比采购单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.AUCTION) {
            auctionService.createFromOrder(order);
            log.info("已联动创建竞价采购单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.SINGLE_SOURCE) {
            singleSourceService.createFromOrder(order);
            log.info("已联动创建单一来源采购单据: orderId={}", order.getId());

        } else if (typeEnum == ProcurementTypeEnum.COLLABORATIVE) {
            cooperativeInnovationService.createFromOrder(order);
            log.info("已联动创建合作创新采购单据: orderId={}", order.getId());

        } else {
            log.debug("采购方式 {} 无需创建额外单据，沿用已有逻辑", procurementType);
        }
    }

    /**
     * 乐观锁自旋扣减库存 — 指数退避重试，最多 3 次，调用 inventory 服务批量扣减接口
     * Optimistic-lock spin-retry inventory deduction — exponential backoff, max 3 retries,
     * calls the inventory service batch deduction endpoint
     *
     * @param orderNo 订单编号（用于关联扣减记录） / order number (used to correlate deduction records)
     * @param items   采购明细列表 / purchase order item list
     * @return true-扣减成功 / false-最终失败 / true if deduction succeeded / false if all attempts failed
     */
    private boolean deductInventoryWithRetry(String orderNo, List<PurchaseOrderItem> items) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                // 构造扣减请求体 / Build deduction request body
                List<Map<String, Object>> deductItems = items.stream().map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("skuId", item.getSkuId());
                    map.put("qty", item.getQuantity());
                    map.put("orderNo", orderNo);
                    return map;
                }).collect(Collectors.toList());

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("items", deductItems);

                // 调用 inventory 服务批量扣减（通过 Nacos 服务发现） / Call inventory service batch deduction (via Nacos service discovery)
                String url = "http://atlas-inventory/api/inventory/deduct";
                @SuppressWarnings("rawtypes")
                Map response = restTemplate.postForObject(url, requestBody, Map.class);

                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    log.info("库存扣减成功: orderNo={} attempt={}", orderNo, attempt + 1);
                    return true;
                }

                log.warn("库存扣减返回失败: orderNo={} attempt={} response={}", orderNo, attempt + 1, response);
            } catch (Exception e) {
                log.warn("库存扣减异常: orderNo={} attempt={} error={}", orderNo, attempt + 1, e.getMessage());
            }

            // 指数退避等待 / Exponential backoff wait
            if (attempt < MAX_RETRY - 1) {
                try {
                    long delay = RETRY_BASE_DELAY_MS * (1L << attempt); // 100, 200, 400
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return false;
    }

    /**
     * 分页查询采购订单 — 支持按供应商/状态/采购方式多条件组合筛选
     * Paginated query of purchase orders — filterable by supplier, status, and procurement type
     *
     * @param supplierId      供应商ID（可选，传 null 则不过滤） / supplier ID (optional, null to skip filter)
     * @param status          订单状态（可选） / order status (optional)
     * @param procurementType 采购方式（可选） / procurement type (optional)
     * @param page            当前页码 / current page number
     * @param size            每页大小 / page size
     * @return 分页结果 / paginated result
     */
    public Page<PurchaseOrder> page(Long supplierId, Integer status, Integer procurementType, int page, int size) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        if (procurementType != null) {
            wrapper.eq(PurchaseOrder::getProcurementType, procurementType);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreatedAt);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 按 ID 查询采购订单 / Query purchase order by ID
     *
     * @param orderId 订单ID / order ID
     * @return 采购订单实体 / purchase order entity
     */
    public PurchaseOrder getById(Long orderId) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    /**
     * 查询订单明细列表 / Query order item list
     *
     * @param orderId 订单ID / order ID
     * @return 明细列表 / item list
     */
    public List<PurchaseOrderItem> listItems(Long orderId) {
        return itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, orderId));
    }

    /**
     * 生成订单编号 — 格式：PO + yyyyMMddHHmmss + 6 位大写随机字符
     * Generate order number — format: PO + yyyyMMddHHmmss + 6-digit uppercase random string
     */
    private String generateOrderNo() {
        return "PO" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
