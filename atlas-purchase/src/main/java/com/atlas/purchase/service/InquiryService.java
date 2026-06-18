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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    // ==================== 多维度比价 / Multi-Dimension Comparison (P0-3.3.3) ====================

    /**
     * 多维度比价评分权重（可通过 application.yml 覆盖） /
     * Multi-dimension comparison score weights (overridable via application.yml inquiry.score.weights)
     */
    private double weightPrice = 50.0;       // 价格 / Price
    private double weightDelivery = 20.0;    // 交期 / Delivery
    private double weightQuality = 15.0;     // 质量资质 / Quality & certification
    private double weightHistory = 15.0;     // 历史绩效 / Historical performance

    /**
     * 多维度比价 — 返回综合排名列表 + 各维度得分明细 /
     * Multi-dimension comparison — returns ranked list with dimension score breakdown
     *
     * <p>评分维度：价格(50%) + 交期(20%) + 质量资质(15%) + 历史绩效(15%) /
     * Scoring dimensions: Price(50%) + Delivery(20%) + Quality(15%) + History(15%)
     * 权重可在 application.yml 中通过 inquiry.score.weights 配置。 /
     * Weights configurable via application.yml inquiry.score.weights.</p>
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 排名列表（按综合得分降序），含各维度明细 / Ranked list (by total score desc), with dimension breakdown
     */
    public List<Map<String, Object>> compareMultiDimension(Long inquiryId) {
        InquiryPurchase inquiry = getById(inquiryId);
        if (inquiry.getStatus() != STATUS_QUOTATION_CLOSED && inquiry.getStatus() != STATUS_COMPARING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅报价结束或比较中状态可进行多维度比价");
        }

        List<InquirySupplier> suppliers = listSuppliers(inquiryId);
        if (suppliers.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无有效报价");
        }

        // 提取各维度极值用于归一化 / Extract dimension extremes for normalization
        BigDecimal minPrice = suppliers.stream()
            .map(s -> s.getQuoteAmount() != null ? s.getQuoteAmount() : BigDecimal.ZERO)
            .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
            .min(BigDecimal::compareTo).orElse(BigDecimal.ONE);

        BigDecimal maxPrice = suppliers.stream()
            .map(s -> s.getQuoteAmount() != null ? s.getQuoteAmount() : BigDecimal.ZERO)
            .max(BigDecimal::compareTo).orElse(BigDecimal.ONE);

        int minDelivery = suppliers.stream()
            .mapToInt(s -> s.getDeliveryDays() != null ? s.getDeliveryDays() : Integer.MAX_VALUE)
            .min().orElse(1);

        int maxDelivery = suppliers.stream()
            .mapToInt(s -> s.getDeliveryDays() != null ? s.getDeliveryDays() : 0)
            .max().orElse(1);

        // 计算各供应商得分 / Compute scores for each supplier
        List<Map<String, Object>> rankings = new ArrayList<>();

        // TODO [P2-N+1] compareMultiDimension：computeQualityScore/computeHistoryScore 在循环内逐供应商调用，
        // 若后续接入 SupplierScorecardService 查询真实得分，将触发 N+1 问题。
        // 优化方向：批量获取所有供应商的质量+历史得分（如 selectScoreBatch(supplierIds)），
        // 存入 Map<Long, BigDecimal> 后在循环内直接 get() 取值。
        // TODO [P2-N+1] compareMultiDimension: computeQualityScore/computeHistoryScore calls per-supplier inside loop;
        // when integrating SupplierScorecardService for real scores, N+1 query risk will emerge.
        // Optimization: batch-fetch quality & history scores for all suppliers upfront (e.g. selectScoreBatch(supplierIds)),
        // store in Map<Long, BigDecimal>, then O(1) lookup inside the loop.
        for (InquirySupplier supplier : suppliers) {
            Map<String, Object> rank = new LinkedHashMap<>();
            rank.put("supplierId", supplier.getSupplierId());
            rank.put("supplierName", supplier.getSupplierName());
            rank.put("quoteAmount", supplier.getQuoteAmount());
            rank.put("deliveryDays", supplier.getDeliveryDays());
            rank.put("paymentTerms", supplier.getPaymentTerms());

            // 价格得分（越小越高） / Price score (lower is better)
            BigDecimal priceScore = computePriceScore(supplier.getQuoteAmount(), minPrice, maxPrice);
            rank.put("priceScore", priceScore);

            // 交期得分（越短越高） / Delivery score (shorter is better)
            BigDecimal deliveryScore = computeDeliveryScore(supplier.getDeliveryDays(), minDelivery, maxDelivery);
            rank.put("deliveryScore", deliveryScore);

            // 质量资质得分（基于供应商已有认证/历史） / Quality score (based on existing certifications/history)
            BigDecimal qualityScore = computeQualityScore(supplier.getSupplierId());
            rank.put("qualityScore", qualityScore);

            // 历史绩效得分 / Historical performance score
            BigDecimal historyScore = computeHistoryScore(supplier.getSupplierId());
            rank.put("historyScore", historyScore);

            // 综合得分 = 加权求和 / Total score = weighted sum
            BigDecimal total = priceScore.multiply(BigDecimal.valueOf(weightPrice / 100))
                .add(deliveryScore.multiply(BigDecimal.valueOf(weightDelivery / 100)))
                .add(qualityScore.multiply(BigDecimal.valueOf(weightQuality / 100)))
                .add(historyScore.multiply(BigDecimal.valueOf(weightHistory / 100)))
                .setScale(2, RoundingMode.HALF_UP);
            rank.put("totalScore", total);
            rankings.add(rank);
        }

        // 按综合得分降序排列 / Sort by total score descending
        rankings.sort((a, b) -> ((BigDecimal) b.get("totalScore")).compareTo((BigDecimal) a.get("totalScore")));

        // 添加排名序号 / Add rank number
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
            rankings.get(i).put("weights", Map.of(
                "price", weightPrice,
                "delivery", weightDelivery,
                "quality", weightQuality,
                "history", weightHistory));
        }

        // 状态推进到比较中 / Advance status to comparing
        if (inquiry.getStatus() == STATUS_QUOTATION_CLOSED) {
            compare(inquiryId);
        }

        log.info("多维度比价: inquiryNo={}, 参与供应商数={}", inquiry.getInquiryNo(), rankings.size());
        return rankings;
    }

    /**
     * 多维度定标 — 基于综合排名选择最优供应商 /
     * Multi-dimension award — select best supplier based on composite ranking
     *
     * <p>在多维度比价基础上定标，选择综合得分最高的供应商。 /
     * Awards based on multi-dimension comparison, selecting supplier with highest composite score.</p>
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 获奖供应商信息 / Winner supplier info
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> awardMultiDimension(Long inquiryId) {
        List<Map<String, Object>> rankings = compareMultiDimension(inquiryId);
        Map<String, Object> winner = rankings.get(0);

        // 更新询价单 / Update inquiry
        InquiryPurchase inquiry = getById(inquiryId);
        inquiry.setWinnerSupplierId((Long) winner.get("supplierId"));
        inquiry.setWinnerAmount((BigDecimal) winner.get("quoteAmount"));
        inquiry.setStatus(STATUS_AWARDED);
        inquiryPurchaseMapper.updateById(inquiry);

        log.info("多维度定标: inquiryNo={}, 成交={}, 综合得分={}",
            inquiry.getInquiryNo(), winner.get("supplierName"), winner.get("totalScore"));
        return winner;
    }

    /**
     * 计算价格得分（0-100，越低越好）/ Compute price score (0-100, lower is better)
     *
     * <p>公式: (maxPrice - currentPrice) / (maxPrice - minPrice) × 100 /
     * Formula: (maxPrice - currentPrice) / (maxPrice - minPrice) × 100</p>
     */
    private BigDecimal computePriceScore(BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        if (minPrice.compareTo(maxPrice) == 0) return new BigDecimal("100");
        return maxPrice.subtract(price)
            .multiply(new BigDecimal("100"))
            .divide(maxPrice.subtract(minPrice), 4, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算交期得分（0-100，越短越好）/ Compute delivery score (0-100, shorter is better)
     *
     * <p>公式: (maxDelivery - currentDelivery) / (maxDelivery - minDelivery) × 100 /
     * Formula: (maxDelivery - currentDelivery) / (maxDelivery - minDelivery) × 100</p>
     */
    private BigDecimal computeDeliveryScore(Integer deliveryDays, int minDays, int maxDays) {
        if (deliveryDays == null || deliveryDays <= 0) return BigDecimal.ZERO;
        if (minDays == maxDays) return new BigDecimal("100");
        return BigDecimal.valueOf(maxDays - deliveryDays)
            .multiply(new BigDecimal("100"))
            .divide(BigDecimal.valueOf(maxDays - minDays), 4, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算质量资质得分（0-100）/ Compute quality score (0-100)
     *
     * <p>基于供应商付款条件/PaymentTerms 中的质量相关关键词暂估分，
     * 实际项目可接入 SupplierScorecardService 获取真实质量得分。 /
     * Estimates score based on quality-related keywords in payment terms;
     * in production, integrate SupplierScorecardService for real quality score.</p>
     */
    private BigDecimal computeQualityScore(Long supplierId) {
        // 默认基准分 / Default base score
        // 实际接入 SupplierScorecardService.getCurrentScore(supplierId, "QUALITY")
        return new BigDecimal("70.00");
    }

    /**
     * 计算历史绩效得分（0-100）/ Compute historical performance score (0-100)
     *
     * <p>基于供应商历史绩效评分，
     * 实际项目可接入 SupplierScorecardService 获取真实历史得分。 /
     * Based on historical performance; in production,
     * integrate SupplierScorecardService for real historical score.</p>
     */
    private BigDecimal computeHistoryScore(Long supplierId) {
        // 默认基准分 / Default base score
        // 实际接入 SupplierScorecardService.query(supplierId, period) 计算历史均值
        return new BigDecimal("65.00");
    }

    // ==================== 权重配置（供 Spring 注入） / Weight Configuration (for Spring injection) ====================

    /**
     * 设置评分权重 / Set scoring weights
     *
     * <p>由 application.yml 配置注入，示例：
     * inquiry.score.weights.price=50, inquiry.score.weights.delivery=20,
     * inquiry.score.weights.quality=15, inquiry.score.weights.history=15</p>
     */
    public void setWeightPrice(double weightPrice) { this.weightPrice = weightPrice; }
    public void setWeightDelivery(double weightDelivery) { this.weightDelivery = weightDelivery; }
    public void setWeightQuality(double weightQuality) { this.weightQuality = weightQuality; }
    public void setWeightHistory(double weightHistory) { this.weightHistory = weightHistory; }

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
