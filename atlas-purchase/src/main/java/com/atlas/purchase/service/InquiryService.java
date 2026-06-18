package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.InquiryPurchase;
import com.atlas.purchase.entity.InquirySupplier;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.InquiryPurchaseMapper;
import com.atlas.purchase.mapper.InquirySupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 询比采购 Service / Inquiry purchase service
 *
 * <p>状态流转：DRAFT(0) → INQUIRING(1) → QUOTATION_CLOSED(2) → COMPARING(3) → AWARDED(4) /
 * Status flow: DRAFT(0) → INQUIRING(1) → QUOTATION_CLOSED(2) → COMPARING(3) → AWARDED(4)
 * <br>任意非终态可跳转到 TERMINATED(5)。 / Any non-terminal state can jump to TERMINATED(5).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    /** 状态常量 / Status constants */
    private static final int STATUS_DRAFT = 0;              // 草稿 / Draft
    private static final int STATUS_INQUIRING = 1;           // 询价中 / Inquiring
    private static final int STATUS_QUOTATION_CLOSED = 2;   // 报价结束 / Quotation closed
    private static final int STATUS_COMPARING = 3;           // 比较中 / Comparing
    private static final int STATUS_AWARDED = 4;             // 已定标 / Awarded
    private static final int STATUS_TERMINATED = 5;          // 已终止 / Terminated

    private final InquiryPurchaseMapper inquiryPurchaseMapper;
    private final InquirySupplierMapper inquirySupplierMapper;

    // ==================== 生命周期 / Lifecycle ====================

    /**
     * 从采购订单创建询比采购 / Create inquiry from purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public InquiryPurchase createFromOrder(PurchaseOrder order) {
        InquiryPurchase inquiry = new InquiryPurchase();
        inquiry.setInquiryNo(generateNo("XJ"));
        inquiry.setPurchaseOrderId(order.getId());
        inquiry.setTitle(order.getTitle());
        inquiry.setMinSupplierCount(3);
        inquiry.setStatus(STATUS_DRAFT);
        inquiryPurchaseMapper.insert(inquiry);
        log.info("创建询比采购: inquiryNo={}", inquiry.getInquiryNo());
        return inquiry;
    }

    /**
     * 发布询价 — 进入询价中 / Publish inquiry — enter inquiring phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long inquiryId, List<Long> supplierIds, List<String> supplierNames,
                         String inquiryContent, LocalDate deadline) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅草稿状态可发布询价");
        }
        if (supplierIds.size() < inquiry.getMinSupplierCount()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                "询价供应商数量不得少于" + inquiry.getMinSupplierCount() + "家");
        }
        for (int i = 0; i < supplierIds.size(); i++) {
            InquirySupplier supplier = new InquirySupplier();
            supplier.setInquiryId(inquiryId);
            supplier.setSupplierId(supplierIds.get(i));
            supplier.setSupplierName(supplierNames.get(i));
            inquirySupplierMapper.insert(supplier);
        }
        inquiry.setInquiryContent(inquiryContent);
        inquiry.setInquiryDeadline(deadline);
        inquiry.setStatus(STATUS_INQUIRING);
        inquiryPurchaseMapper.updateById(inquiry);
        log.info("发布询价: inquiryNo={}, 供应商数={}", inquiry.getInquiryNo(), supplierIds.size());
    }

    /**
     * 供应商报价 / Supplier submits quote
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitQuote(Long inquirySupplierId, BigDecimal quoteAmount,
                             Integer deliveryDays, String paymentTerms, String remark) {
        InquirySupplier supplier = inquirySupplierMapper.selectById(inquirySupplierId);
        if (supplier == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "询价记录不存在");
        }
        InquiryPurchase inquiry = getById(supplier.getInquiryId());
        if (inquiry.getStatus() != STATUS_INQUIRING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前非询价阶段");
        }
        if (inquiry.getInquiryDeadline() != null && LocalDate.now().isAfter(inquiry.getInquiryDeadline())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "报价已截止");
        }
        supplier.setQuoteAmount(quoteAmount);
        supplier.setDeliveryDays(deliveryDays);
        supplier.setPaymentTerms(paymentTerms);
        supplier.setRemark(remark);
        supplier.setQuoteTime(LocalDateTime.now());
        inquirySupplierMapper.updateById(supplier);
        log.info("供应商报价: supplier={}, amount={}", supplier.getSupplierName(), quoteAmount);
    }

    /**
     * 关闭报价 — 进入报价结束 / Close quotation — enter quotation closed phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeQuotation(Long inquiryId) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() != STATUS_INQUIRING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅询价中状态可关闭报价");
        }
        inquiry.setStatus(STATUS_QUOTATION_CLOSED);
        inquiryPurchaseMapper.updateById(inquiry);
        log.info("关闭报价: inquiryNo={}", inquiry.getInquiryNo());
    }

    /**
     * 比较报价 — 进入比较中，综合比较价格/交期/付款条件 /
     * Compare quotations — enter comparing phase, comprehensive comparison of price / delivery / payment terms
     */
    @Transactional(rollbackFor = Exception.class)
    public void compare(Long inquiryId) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() != STATUS_QUOTATION_CLOSED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅报价结束状态可进入比较");
        }
        inquiry.setStatus(STATUS_COMPARING);
        inquiryPurchaseMapper.updateById(inquiry);
        log.info("进入报价比较: inquiryNo={}", inquiry.getInquiryNo());
    }

    /**
     * 定标 — 选择报价最低的供应商 / Award — select the supplier with lowest quote
     */
    @Transactional(rollbackFor = Exception.class)
    public void award(Long inquiryId) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() != STATUS_COMPARING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅比较中状态可定标");
        }
        List<InquirySupplier> suppliers = inquirySupplierMapper.selectList(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, inquiryId)
                .isNotNull(InquirySupplier::getQuoteAmount));
        if (suppliers.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无有效报价");
        }
        InquirySupplier winner = suppliers.stream()
            .min(Comparator.comparing(s -> s.getQuoteAmount() != null ? s.getQuoteAmount() : BigDecimal.valueOf(Long.MAX_VALUE)))
            .orElseThrow(() -> new BizException(ErrorCode.DATA_NOT_FOUND, "无法确定成交供应商"));
        inquiry.setWinnerSupplierId(winner.getSupplierId());
        inquiry.setWinnerAmount(winner.getQuoteAmount());
        inquiry.setStatus(STATUS_AWARDED);
        inquiryPurchaseMapper.updateById(inquiry);
        log.info("定标: inquiryNo={}, 成交={}, 金额={}", inquiry.getInquiryNo(), winner.getSupplierName(), winner.getQuoteAmount());
    }

    /**
     * 终止 / Terminate
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long inquiryId) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() == STATUS_AWARDED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已定标不可终止");
        }
        inquiry.setStatus(STATUS_TERMINATED);
        inquiryPurchaseMapper.updateById(inquiry);
        log.info("终止询比采购: inquiryNo={}", inquiry.getInquiryNo());
    }

    // ==================== 查询 / Query ====================

    /**
     * 按主键查询 / Query by primary key
     */
    public InquiryPurchase getById(Long id) {
        InquiryPurchase inquiry = inquiryPurchaseMapper.selectById(id);
        if (inquiry == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "询比采购记录不存在: " + id);
        }
        return inquiry;
    }

    /**
     * 分页查询（支持 keyword + status 筛选） / Paginated query (supports keyword + status filtering)
     */
    public IPage<InquiryPurchase> page(IPage<InquiryPurchase> page, String keyword, Integer status) {
        LambdaQueryWrapper<InquiryPurchase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(InquiryPurchase::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(InquiryPurchase::getStatus, status);
        }
        wrapper.orderByDesc(InquiryPurchase::getCreatedAt);
        return inquiryPurchaseMapper.selectPage(page, wrapper);
    }

    /**
     * 查询询价的供应商报价列表 / Query supplier quote list for an inquiry
     */
    public List<InquirySupplier> listSuppliers(Long inquiryId) {
        return inquirySupplierMapper.selectList(
            new LambdaQueryWrapper<InquirySupplier>()
                .eq(InquirySupplier::getInquiryId, inquiryId)
                .orderByAsc(InquirySupplier::getQuoteAmount));
    }

    /**
     * 生成编号 / Generate serial number
     */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
