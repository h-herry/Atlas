package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.OpenBidding;
import com.atlas.purchase.entity.OpenBiddingSupplier;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.OpenBiddingMapper;
import com.atlas.purchase.mapper.OpenBiddingSupplierMapper;
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
 * 公开招标采购 Service — 完整招标流程 / Open bidding procurement service — full bidding workflow
 *
 * <p>状态流转：PREPARING(0) → ANNOUNCEMENT(1) → BIDDING(2) → OPENING(3) → EVALUATING(4) → AWARDED(5) /
 * Status flow: PREPARING(0) → ANNOUNCEMENT(1) → BIDDING(2) → OPENING(3) → EVALUATING(4) → AWARDED(5)
 * <br>任意非终态可跳转到 FLOW_FAILED(6) / TERMINATED(7)。 / Any non-terminal state can jump to FLOW_FAILED(6) / TERMINATED(7).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenBiddingService {

    /** 招标状态常量 / Bidding status constants */
    private static final int STATUS_PREPARING = 0;
    private static final int STATUS_ANNOUNCEMENT = 1;
    private static final int STATUS_BIDDING = 2;
    private static final int STATUS_OPENING = 3;
    private static final int STATUS_EVALUATING = 4;
    private static final int STATUS_AWARDED = 5;
    private static final int STATUS_FLOW_FAILED = 6;
    private static final int STATUS_TERMINATED = 7;

    /** 资格审查状态常量 / Qualification review status constants */
    private static final int QUALIFIED_PENDING = 0;
    private static final int QUALIFIED_PASS = 1;
    private static final int QUALIFIED_FAIL = 2;

    private final OpenBiddingMapper openBiddingMapper;
    private final OpenBiddingSupplierMapper openBiddingSupplierMapper;

    // ==================== 生命周期管理 / Lifecycle Management ====================

    /**
     * 从采购订单创建公开招标 / Create open bidding from purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public OpenBidding createFromOrder(PurchaseOrder order) {
        OpenBidding bidding = new OpenBidding();
        bidding.setBidNo(generateNo("ZB"));
        bidding.setPurchaseOrderId(order.getId());
        bidding.setTitle(order.getTitle());
        bidding.setBudgetAmount(order.getTotalAmount());
        bidding.setEvaluationMethod("MIN_PRICE");
        bidding.setBidDeposit(BigDecimal.ZERO);
        bidding.setStatus(STATUS_PREPARING);
        openBiddingMapper.insert(bidding);
        log.info("创建公开招标: bidNo={}, title={}", bidding.getBidNo(), bidding.getTitle());
        return bidding;
    }

    /**
     * 发布招标公告 / Publish bidding announcement
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long biddingId, Long publisherId, LocalDate bidEndDate, LocalDate bidOpeningDate, String bidContent) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_PREPARING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅准备中状态的招标可发布");
        }
        if (bidEndDate != null && bidOpeningDate != null && bidOpeningDate.isBefore(bidEndDate)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "开标日期不能早于投标截止日期");
        }
        bidding.setStatus(STATUS_ANNOUNCEMENT);
        if (bidContent != null) bidding.setBidContent(bidContent);
        bidding.setBidStartDate(LocalDate.now());
        bidding.setBidEndDate(bidEndDate);
        bidding.setBidOpeningDate(bidOpeningDate);
        bidding.setPublisherId(publisherId);
        openBiddingMapper.updateById(bidding);
        log.info("发布招标公告: bidNo={}, 截止={}, 开标={}", bidding.getBidNo(), bidEndDate, bidOpeningDate);
    }

    /**
     * 开始投标 — 从公告期进入投标中 / Start bidding — from announcement to bidding
     */
    @Transactional(rollbackFor = Exception.class)
    public void startBidding(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_ANNOUNCEMENT) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅公告期状态的招标可进入投标中");
        }
        bidding.setStatus(STATUS_BIDDING);
        openBiddingMapper.updateById(bidding);
        log.info("开始投标: bidNo={}", bidding.getBidNo());
    }

    /**
     * 供应商投标 / Supplier submits bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitBid(Long biddingId, Long supplierId, String supplierName, BigDecimal bidAmount, String bidFileUrl) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_BIDDING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前非投标阶段");
        }
        if (LocalDate.now().isAfter(bidding.getBidEndDate())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "投标已截止");
        }
        OpenBiddingSupplier supplier = new OpenBiddingSupplier();
        supplier.setBiddingId(biddingId);
        supplier.setSupplierId(supplierId);
        supplier.setSupplierName(supplierName);
        supplier.setBidAmount(bidAmount);
        supplier.setBidFileUrl(bidFileUrl);
        supplier.setSubmitTime(LocalDateTime.now());
        supplier.setIsQualified(QUALIFIED_PENDING);
        openBiddingSupplierMapper.insert(supplier);
        log.info("供应商投标: bidNo={}, supplier={}, amount={}", bidding.getBidNo(), supplierName, bidAmount);
    }

    /**
     * 开标 / Open bids
     */
    @Transactional(rollbackFor = Exception.class)
    public void openBid(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_BIDDING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅投标中状态可开标");
        }
        long count = openBiddingSupplierMapper.selectCount(
            new LambdaQueryWrapper<OpenBiddingSupplier>().eq(OpenBiddingSupplier::getBiddingId, biddingId));
        if (count < 3) {
            log.warn("投标供应商不足3家，流标处理: bidNo={}, count={}", bidding.getBidNo(), count);
            bidding.setStatus(STATUS_FLOW_FAILED);
            openBiddingMapper.updateById(bidding);
            return;
        }
        // 资格审核：批量设为合格 / Qualification review: batch set qualified
        List<OpenBiddingSupplier> suppliers = openBiddingSupplierMapper.selectList(
            new LambdaQueryWrapper<OpenBiddingSupplier>().eq(OpenBiddingSupplier::getBiddingId, biddingId));
        for (OpenBiddingSupplier s : suppliers) {
            s.setIsQualified(QUALIFIED_PASS);
            openBiddingSupplierMapper.updateById(s);
        }
        bidding.setStatus(STATUS_OPENING);
        openBiddingMapper.updateById(bidding);
        log.info("开标完成: bidNo={}, 合格供应商数={}", bidding.getBidNo(), count);
    }

    /**
     * 评标 / Evaluate bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long biddingId, BigDecimal score, String comment, Long supplierRecordId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_OPENING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅开标中状态可评标");
        }
        OpenBiddingSupplier supplier = openBiddingSupplierMapper.selectById(supplierRecordId);
        if (supplier == null || !supplier.getBiddingId().equals(biddingId)) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商投标记录不存在");
        }
        supplier.setEvalScore(score);
        supplier.setEvalComment(comment);
        openBiddingSupplierMapper.updateById(supplier);
    }

    /**
     * 提交评标结果 → 进入评标中 / Submit evaluation results → enter evaluation phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void startEvaluation(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_OPENING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅开标中状态可进入评标中");
        }
        bidding.setStatus(STATUS_EVALUATING);
        openBiddingMapper.updateById(bidding);
        log.info("进入评标中: bidNo={}", bidding.getBidNo());
    }

    /**
     * 定标 — 根据评标办法确定中标供应商 / Award — determine winner by evaluation method
     */
    @Transactional(rollbackFor = Exception.class)
    public void award(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_EVALUATING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅评标中状态可定标");
        }
        List<OpenBiddingSupplier> suppliers = openBiddingSupplierMapper.selectList(
            new LambdaQueryWrapper<OpenBiddingSupplier>()
                .eq(OpenBiddingSupplier::getBiddingId, biddingId)
                .eq(OpenBiddingSupplier::getIsQualified, QUALIFIED_PASS));
        if (suppliers.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无合格供应商可供定标");
        }
        OpenBiddingSupplier winner;
        String method = bidding.getEvaluationMethod();
        if ("SCORE".equals(method)) {
            // 综合评分法 — 得分最高 / Comprehensive scoring — highest score wins
            winner = suppliers.stream().max(Comparator.comparing(s ->
                s.getEvalScore() != null ? s.getEvalScore() : BigDecimal.ZERO)).orElse(null);
        } else if ("BEST_VALUE".equals(method)) {
            // 性价比法 — 得分/价格最高 / Best value — highest score-to-price ratio
            winner = suppliers.stream().max(Comparator.comparing(s -> {
                BigDecimal score = s.getEvalScore() != null ? s.getEvalScore() : BigDecimal.ZERO;
                BigDecimal price = s.getBidAmount() != null ? s.getBidAmount() : BigDecimal.ONE;
                return score.divide(price, 6, java.math.RoundingMode.HALF_UP);
            })).orElse(null);
        } else {
            // 最低价法（默认MIN_PRICE） / Lowest price (default MIN_PRICE)
            winner = suppliers.stream().min(Comparator.comparing(s ->
                s.getBidAmount() != null ? s.getBidAmount() : BigDecimal.valueOf(Long.MAX_VALUE))).orElse(null);
        }
        if (winner == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无法确定中标供应商");
        }
        bidding.setWinnerSupplierId(winner.getSupplierId());
        bidding.setWinnerAmount(winner.getBidAmount());
        bidding.setStatus(STATUS_AWARDED);
        openBiddingMapper.updateById(bidding);
        log.info("定标完成: bidNo={}, 中标={}, 金额={}, 评标办法={}",
            bidding.getBidNo(), winner.getSupplierName(), winner.getBidAmount(), method);
    }

    /**
     * 流标 / Bid failure (insufficient participants)
     */
    @Transactional(rollbackFor = Exception.class)
    public void flowBid(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() == STATUS_AWARDED || bidding.getStatus() == STATUS_TERMINATED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已定标或已终止的招标不可流标");
        }
        bidding.setStatus(STATUS_FLOW_FAILED);
        openBiddingMapper.updateById(bidding);
        log.info("招标流标: bidNo={}", bidding.getBidNo());
    }

    /**
     * 终止招标 / Terminate bidding
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long biddingId) {
        OpenBidding bidding = getById(biddingId);
        if (bidding.getStatus() == STATUS_AWARDED || bidding.getStatus() == STATUS_FLOW_FAILED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已定标或已流标的招标不可终止");
        }
        bidding.setStatus(STATUS_TERMINATED);
        openBiddingMapper.updateById(bidding);
        log.info("终止招标: bidNo={}", bidding.getBidNo());
    }

    // ==================== 查询 / Query ====================

    public OpenBidding getById(Long id) {
        OpenBidding bidding = openBiddingMapper.selectById(id);
        if (bidding == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "公开招标记录不存在: " + id);
        }
        return bidding;
    }

    /**
     * 分页查询 / Paginated query
     */
    public IPage<OpenBidding> page(IPage<OpenBidding> page, String keyword, Integer status) {
        LambdaQueryWrapper<OpenBidding> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(OpenBidding::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(OpenBidding::getStatus, status);
        }
        wrapper.orderByDesc(OpenBidding::getCreatedAt);
        return openBiddingMapper.selectPage(page, wrapper);
    }

    /**
     * 查询供应商列表 / Query supplier list
     */
    public List<OpenBiddingSupplier> listSuppliers(Long biddingId) {
        return openBiddingSupplierMapper.selectList(
            new LambdaQueryWrapper<OpenBiddingSupplier>()
                .eq(OpenBiddingSupplier::getBiddingId, biddingId)
                .orderByAsc(OpenBiddingSupplier::getBidAmount));
    }

    // ==================== 工具方法 / Utility ====================

    /** 生成编号 / Generate number */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
