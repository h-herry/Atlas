package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.InvitedBidding;
import com.atlas.purchase.entity.InvitedBiddingSupplier;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.InvitedBiddingMapper;
import com.atlas.purchase.mapper.InvitedBiddingSupplierMapper;
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
 * 邀请招标采购 Service / Invited bidding procurement service
 *
 * <p>状态流转：PREPARING(0) → INVITING(1) → BIDDING(2) → OPENING(3) → EVALUATING(4) → AWARDED(5) /
 * Status flow: PREPARING(0) → INVITING(1) → BIDDING(2) → OPENING(3) → EVALUATING(4) → AWARDED(5)
 * <br>任意非终态可跳转到 TERMINATED(6)。 / Any non-terminal state can jump to TERMINATED(6).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvitedBiddingService {

    private static final int STATUS_PREPARING = 0;
    private static final int STATUS_INVITING = 1;
    private static final int STATUS_BIDDING = 2;
    private static final int STATUS_OPENING = 3;
    private static final int STATUS_EVALUATING = 4;
    private static final int STATUS_AWARDED = 5;
    private static final int STATUS_TERMINATED = 6;

    /** 邀请状态常量 / Invite status constants */
    private static final int INVITE_SENT = 0;
    private static final int INVITE_ACCEPTED = 1;
    private static final int INVITE_REJECTED = 2;

    private final InvitedBiddingMapper invitedBiddingMapper;
    private final InvitedBiddingSupplierMapper invitedBiddingSupplierMapper;

    // ==================== 生命周期 / Lifecycle ====================

    /**
     * 从采购订单创建邀请招标 / Create invited bidding from purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public InvitedBidding createFromOrder(PurchaseOrder order) {
        InvitedBidding bidding = new InvitedBidding();
        bidding.setBidNo(generateNo("YZ"));
        bidding.setPurchaseOrderId(order.getId());
        bidding.setTitle(order.getTitle());
        bidding.setBudgetAmount(order.getTotalAmount());
        bidding.setMinInviteCount(3);
        bidding.setStatus(STATUS_PREPARING);
        invitedBiddingMapper.insert(bidding);
        log.info("创建邀请招标: bidNo={}", bidding.getBidNo());
        return bidding;
    }

    /**
     * 邀请供应商 — 进入邀请中状态 / Invite suppliers — enter inviting status
     */
    @Transactional(rollbackFor = Exception.class)
    public void inviteSuppliers(Long biddingId, List<Long> supplierIds, List<String> supplierNames,
                                 String invitationReason, LocalDate bidEndDate, LocalDate bidOpeningDate) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_PREPARING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅准备中状态可发送邀请");
        }
        if (supplierIds.size() < bidding.getMinInviteCount()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                "邀请供应商数量不得少于" + bidding.getMinInviteCount() + "家");
        }
        for (int i = 0; i < supplierIds.size(); i++) {
            InvitedBiddingSupplier supplier = new InvitedBiddingSupplier();
            supplier.setBiddingId(biddingId);
            supplier.setSupplierId(supplierIds.get(i));
            supplier.setSupplierName(supplierNames.get(i));
            supplier.setInviteStatus(INVITE_SENT);
            invitedBiddingSupplierMapper.insert(supplier);
        }
        bidding.setInvitationReason(invitationReason);
        bidding.setBidEndDate(bidEndDate);
        bidding.setBidOpeningDate(bidOpeningDate);
        bidding.setStatus(STATUS_INVITING);
        invitedBiddingMapper.updateById(bidding);
        log.info("发送邀请: bidNo={}, 邀请数量={}", bidding.getBidNo(), supplierIds.size());
    }

    /**
     * 供应商接受邀请 / Supplier accepts invitation
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptInvite(Long supplierRecordId) {
        InvitedBiddingSupplier supplier = invitedBiddingSupplierMapper.selectById(supplierRecordId);
        if (supplier == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "邀请记录不存在");
        }
        if (supplier.getInviteStatus() != INVITE_SENT) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前邀请状态不可接受");
        }
        supplier.setInviteStatus(INVITE_ACCEPTED);
        invitedBiddingSupplierMapper.updateById(supplier);
        log.info("供应商接受邀请: supplier={}", supplier.getSupplierName());
    }

    /**
     * 供应商拒绝邀请 / Supplier rejects invitation
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectInvite(Long supplierRecordId) {
        InvitedBiddingSupplier supplier = invitedBiddingSupplierMapper.selectById(supplierRecordId);
        if (supplier == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "邀请记录不存在");
        }
        if (supplier.getInviteStatus() != INVITE_SENT) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前邀请状态不可拒绝");
        }
        supplier.setInviteStatus(INVITE_REJECTED);
        invitedBiddingSupplierMapper.updateById(supplier);
        log.info("供应商拒绝邀请: supplier={}", supplier.getSupplierName());
    }

    /**
     * 开始投标 — 进入投标中 / Start bidding — enter bidding phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void startBidding(Long biddingId) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_INVITING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅邀请中状态可进入投标中");
        }
        long acceptedCount = invitedBiddingSupplierMapper.selectCount(
            new LambdaQueryWrapper<InvitedBiddingSupplier>()
                .eq(InvitedBiddingSupplier::getBiddingId, biddingId)
                .eq(InvitedBiddingSupplier::getInviteStatus, INVITE_ACCEPTED));
        if (acceptedCount < bidding.getMinInviteCount()) {
            throw new BizException(ErrorCode.ILLEGAL_STATE,
                "接受邀请的供应商不足" + bidding.getMinInviteCount() + "家");
        }
        bidding.setStatus(STATUS_BIDDING);
        invitedBiddingMapper.updateById(bidding);
        log.info("开始投标: bidNo={}, 接受邀请数={}", bidding.getBidNo(), acceptedCount);
    }

    /**
     * 供应商投标 / Supplier submits bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitBid(Long supplierRecordId, BigDecimal bidAmount, String bidFileUrl) {
        InvitedBiddingSupplier supplier = invitedBiddingSupplierMapper.selectById(supplierRecordId);
        if (supplier == null || supplier.getInviteStatus() != INVITE_ACCEPTED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "未接受邀请或记录不存在");
        }
        InvitedBidding bidding = getById(supplier.getBiddingId());
        if (bidding.getStatus() != STATUS_BIDDING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前非投标阶段");
        }
        if (bidding.getBidEndDate() != null && LocalDate.now().isAfter(bidding.getBidEndDate())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "投标已截止");
        }
        supplier.setBidAmount(bidAmount);
        supplier.setBidFileUrl(bidFileUrl);
        supplier.setSubmitTime(LocalDateTime.now());
        invitedBiddingSupplierMapper.updateById(supplier);
        log.info("供应商投标: supplier={}, amount={}", supplier.getSupplierName(), bidAmount);
    }

    /**
     * 开标 / Open bids
     */
    @Transactional(rollbackFor = Exception.class)
    public void openBid(Long biddingId) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_BIDDING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅投标中状态可开标");
        }
        bidding.setStatus(STATUS_OPENING);
        invitedBiddingMapper.updateById(bidding);
        log.info("开标: bidNo={}", bidding.getBidNo());
    }

    /**
     * 评标 / Evaluate bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long supplierRecordId, BigDecimal score) {
        InvitedBiddingSupplier supplier = invitedBiddingSupplierMapper.selectById(supplierRecordId);
        if (supplier == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商投标记录不存在");
        }
        InvitedBidding bidding = getById(supplier.getBiddingId());
        if (bidding.getStatus() != STATUS_OPENING && bidding.getStatus() != STATUS_EVALUATING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前状态不可评标");
        }
        supplier.setEvalScore(score);
        invitedBiddingSupplierMapper.updateById(supplier);
    }

    /**
     * 进入评标中 / Enter evaluation phase
     */
    @Transactional(rollbackFor = Exception.class)
    public void startEvaluation(Long biddingId) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_OPENING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅开标中状态可进入评标中");
        }
        bidding.setStatus(STATUS_EVALUATING);
        invitedBiddingMapper.updateById(bidding);
    }

    /**
     * 定标 — 最低价中标（邀请招标默认最低价法） /
     * Award — lowest price wins (invited bidding defaults to lowest price method)
     */
    @Transactional(rollbackFor = Exception.class)
    public void award(Long biddingId) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() != STATUS_EVALUATING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅评标中状态可定标");
        }
        List<InvitedBiddingSupplier> suppliers = invitedBiddingSupplierMapper.selectList(
            new LambdaQueryWrapper<InvitedBiddingSupplier>()
                .eq(InvitedBiddingSupplier::getBiddingId, biddingId)
                .eq(InvitedBiddingSupplier::getInviteStatus, INVITE_ACCEPTED)
                .isNotNull(InvitedBiddingSupplier::getBidAmount));
        if (suppliers.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无有效投标供应商");
        }
        InvitedBiddingSupplier winner = suppliers.stream()
            .min(Comparator.comparing(s -> s.getBidAmount() != null ? s.getBidAmount() : BigDecimal.valueOf(Long.MAX_VALUE)))
            .orElseThrow(() -> new BizException(ErrorCode.DATA_NOT_FOUND, "无法确定中标供应商"));
        bidding.setWinnerSupplierId(winner.getSupplierId());
        bidding.setWinnerAmount(winner.getBidAmount());
        bidding.setStatus(STATUS_AWARDED);
        invitedBiddingMapper.updateById(bidding);
        log.info("定标: bidNo={}, 中标={}, 金额={}", bidding.getBidNo(), winner.getSupplierName(), winner.getBidAmount());
    }

    /**
     * 终止 / Terminate
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long biddingId) {
        InvitedBidding bidding = getById(biddingId);
        if (bidding.getStatus() == STATUS_AWARDED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已定标不可终止");
        }
        bidding.setStatus(STATUS_TERMINATED);
        invitedBiddingMapper.updateById(bidding);
        log.info("终止邀请招标: bidNo={}", bidding.getBidNo());
    }

    // ==================== 查询 / Query ====================

    public InvitedBidding getById(Long id) {
        InvitedBidding bidding = invitedBiddingMapper.selectById(id);
        if (bidding == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "邀请招标记录不存在: " + id);
        }
        return bidding;
    }

    /**
     * 分页查询 / Paginated query
     */
    public IPage<InvitedBidding> page(IPage<InvitedBidding> page, String keyword, Integer status) {
        LambdaQueryWrapper<InvitedBidding> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(InvitedBidding::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(InvitedBidding::getStatus, status);
        }
        wrapper.orderByDesc(InvitedBidding::getCreatedAt);
        return invitedBiddingMapper.selectPage(page, wrapper);
    }

    /**
     * 查询供应商列表 / Query supplier list
     */
    public List<InvitedBiddingSupplier> listSuppliers(Long biddingId) {
        return invitedBiddingSupplierMapper.selectList(
            new LambdaQueryWrapper<InvitedBiddingSupplier>()
                .eq(InvitedBiddingSupplier::getBiddingId, biddingId));
    }

    /** 生成编号 / Generate number */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
