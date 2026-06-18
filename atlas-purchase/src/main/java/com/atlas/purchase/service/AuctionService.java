package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.AuctionBid;
import com.atlas.purchase.entity.AuctionPurchase;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.AuctionBidMapper;
import com.atlas.purchase.mapper.AuctionPurchaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 竞价采购业务服务 — 反向拍卖模式 / Auction service — reverse auction model
 *
 * <p>状态流转：PREPARING(0) → AUCTIONING(1) → ENDED(2) → AWARDED(3) /
 * Status flow: PREPARING(0) → AUCTIONING(1) → ENDED(2) → AWARDED(3)
 * <br>任意非终态可跳转到 TERMINATED(4)。 / Any non-terminal state can jump to TERMINATED(4).
 * <br>支持自动延时：最后时刻出价自动延长结束时间。 / Supports auto-extension: last-minute bid extends the end time.
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    // 状态常量 / Status constants
    private static final int STATUS_PREPARING = 0;   // 准备中 / Preparing
    private static final int STATUS_AUCTIONING = 1;  // 竞价中 / Auctioning
    private static final int STATUS_ENDED = 2;       // 已结束 / Ended
    private static final int STATUS_AWARDED = 3;     // 已定标 / Awarded
    private static final int STATUS_TERMINATED = 4;  // 已终止 / Terminated

    private final AuctionPurchaseMapper auctionPurchaseMapper;
    private final AuctionBidMapper auctionBidMapper;

    // ==================== 生命周期 / Lifecycle ====================

    /**
     * 从采购订单创建竞价 / Create auction from purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public AuctionPurchase createFromOrder(PurchaseOrder order) {
        AuctionPurchase auction = new AuctionPurchase();
        auction.setAuctionNo(generateNo("JJ"));
        auction.setPurchaseOrderId(order.getId());
        auction.setTitle(order.getTitle());
        auction.setStartPrice(order.getTotalAmount());
        auction.setMinDecrement(BigDecimal.valueOf(100));
        auction.setAuctionType("REVERSE");
        auction.setAutoExtend(0);
        auction.setExtendMinutes(5);
        auction.setStatus(STATUS_PREPARING);
        auctionPurchaseMapper.insert(auction);
        log.info("创建竞价采购: auctionNo={}", auction.getAuctionNo());
        return auction;
    }

    /**
     * 开始竞价 / Start auction
     */
    @Transactional(rollbackFor = Exception.class)
    public void start(Long auctionId, LocalDateTime endTime) {
        AuctionPurchase auction = getById(auctionId);
        if (auction.getStatus() != STATUS_PREPARING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅准备中状态可开始竞价 / Only PREPARING status can start auction");
        }
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(endTime);
        auction.setStatus(STATUS_AUCTIONING);
        auctionPurchaseMapper.updateById(auction);
        log.info("开始竞价: auctionNo={}, 结束时间={}", auction.getAuctionNo(), endTime);
    }

    /**
     * 供应商出价 / Supplier places bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void placeBid(Long auctionId, Long supplierId, String supplierName, BigDecimal bidAmount) {
        AuctionPurchase auction = getById(auctionId);
        if (auction.getStatus() != STATUS_AUCTIONING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "当前非竞价阶段 / Not in auction phase");
        }
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "竞价已结束 / Auction already ended");
        }
        // 反拍：出价须低于起拍价，且降幅不小于最小幅度 / Reverse: bid must be below start price
        if ("REVERSE".equals(auction.getAuctionType())) {
            if (bidAmount.compareTo(auction.getStartPrice()) >= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "出价须低于起拍价 / Bid must be below start price");
            }
            // 检查是否有上一轮出价，出价须低于上一轮 / Check previous bid, must be lower
            AuctionBid lastBid = auctionBidMapper.selectList(
                new LambdaQueryWrapper<AuctionBid>()
                    .eq(AuctionBid::getAuctionId, auctionId)
                    .eq(AuctionBid::getSupplierId, supplierId)
                    .eq(AuctionBid::getIsValid, 1)
                    .orderByDesc(AuctionBid::getBidTime)
                    .last("LIMIT 1")).stream().findFirst().orElse(null);
            if (lastBid != null && bidAmount.compareTo(lastBid.getBidAmount()) >= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "出价须低于上一轮报价 / Bid must be below last round");
            }
        }
        AuctionBid bid = new AuctionBid();
        bid.setAuctionId(auctionId);
        bid.setSupplierId(supplierId);
        bid.setSupplierName(supplierName);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(LocalDateTime.now());
        bid.setIsValid(1);
        auctionBidMapper.insert(bid);
        // 自动延时：最后 extendMinutes 分钟内出价则延长 / Auto-extend: extend if bid in last N minutes
        if (auction.getAutoExtend() != null && auction.getAutoExtend() == 1) {
            LocalDateTime extendThreshold = auction.getEndTime().minusMinutes(auction.getExtendMinutes());
            if (LocalDateTime.now().isAfter(extendThreshold)) {
                auction.setEndTime(auction.getEndTime().plusMinutes(auction.getExtendMinutes()));
                auctionPurchaseMapper.updateById(auction);
                log.info("自动延时: auctionNo={}, 新结束时间={}", auction.getAuctionNo(), auction.getEndTime());
            }
        }
        log.info("出价: auctionNo={}, supplier={}, amount={}", auction.getAuctionNo(), supplierName, bidAmount);
    }

    /**
     * 结束竞价 / End auction
     */
    @Transactional(rollbackFor = Exception.class)
    public void end(Long auctionId) {
        AuctionPurchase auction = getById(auctionId);
        if (auction.getStatus() != STATUS_AUCTIONING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅竞价中状态可结束 / Only AUCTIONING status can end");
        }
        auction.setStatus(STATUS_ENDED);
        auctionPurchaseMapper.updateById(auction);
        log.info("竞价结束: auctionNo={}", auction.getAuctionNo());
    }

    /**
     * 定标 — 反向竞价以最低出价中标 / Award — reverse auction awards lowest bid
     */
    @Transactional(rollbackFor = Exception.class)
    public void award(Long auctionId) {
        AuctionPurchase auction = getById(auctionId);
        if (auction.getStatus() != STATUS_ENDED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅已结束状态可定标 / Only ENDED status can award");
        }
        List<AuctionBid> bids = auctionBidMapper.selectList(
            new LambdaQueryWrapper<AuctionBid>()
                .eq(AuctionBid::getAuctionId, auctionId)
                .eq(AuctionBid::getIsValid, 1));
        if (bids.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "无有效出价记录 / No valid bids");
        }
        AuctionBid winner = bids.stream()
            .min(Comparator.comparing(b -> b.getBidAmount() != null ? b.getBidAmount() : BigDecimal.valueOf(Long.MAX_VALUE)))
            .orElseThrow(() -> new BizException(ErrorCode.DATA_NOT_FOUND, "无法确定成交供应商 / Cannot determine winner"));
        auction.setWinnerSupplierId(winner.getSupplierId());
        auction.setWinnerAmount(winner.getBidAmount());
        auction.setStatus(STATUS_AWARDED);
        auctionPurchaseMapper.updateById(auction);
        log.info("定标: auctionNo={}, 成交={}, 金额={}", auction.getAuctionNo(), winner.getSupplierName(), winner.getBidAmount());
    }

    /**
     * 终止竞价 / Terminate auction
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long auctionId) {
        AuctionPurchase auction = getById(auctionId);
        if (auction.getStatus() == STATUS_AWARDED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已定标不可终止 / Awarded auctions cannot be terminated");
        }
        auction.setStatus(STATUS_TERMINATED);
        auctionPurchaseMapper.updateById(auction);
        log.info("终止竞价: auctionNo={}", auction.getAuctionNo());
    }

    // ==================== 查询 / Query ====================

    /**
     * 查询竞价详情 / Query auction detail
     */
    public AuctionPurchase getById(Long id) {
        AuctionPurchase auction = auctionPurchaseMapper.selectById(id);
        if (auction == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "竞价采购记录不存在: " + id);
        }
        return auction;
    }

    /**
     * 分页查询竞价列表 / Paginated query of auction list
     */
    public IPage<AuctionPurchase> page(IPage<AuctionPurchase> page, String keyword, Integer status) {
        LambdaQueryWrapper<AuctionPurchase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(AuctionPurchase::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(AuctionPurchase::getStatus, status);
        }
        wrapper.orderByDesc(AuctionPurchase::getCreatedAt);
        return auctionPurchaseMapper.selectPage(page, wrapper);
    }

    /**
     * 查询出价记录（按金额升序） / Query bid records (sorted ascending by amount)
     */
    public List<AuctionBid> listBids(Long auctionId) {
        return auctionBidMapper.selectList(
            new LambdaQueryWrapper<AuctionBid>()
                .eq(AuctionBid::getAuctionId, auctionId)
                .orderByAsc(AuctionBid::getBidAmount));
    }

    /**
     * 生成编号 / Generate serial number
     */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
