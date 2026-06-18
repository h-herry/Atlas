package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.config.SupplierSecurityConfig;
import com.atlas.supplier.dto.portal.BidHistoryResponse;
import com.atlas.supplier.dto.portal.BidRequest;
import com.atlas.supplier.dto.portal.BiddingRoomResponse;
import com.atlas.supplier.dto.portal.ComparisonResultResponse;
import com.atlas.supplier.dto.portal.InquiryViewResponse;
import com.atlas.supplier.dto.portal.QuotationSubmitRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 询报价服务（供应商端） — 供应商查看企业询价单、提交报价、查看中标结果 /
 * Inquiry & quotation service (portal) — supplier views enterprise inquiries, submits quotes, views award results
 *
 * <p>复用 atlas-purchase 模块中已有的询价/报价相关表（Bidding / Inquiry / PurchaseOrderItem），通过 supplier_id 做数据隔离。 /
 * Reuses existing inquiry/quotation tables from atlas-purchase module (Bidding / Inquiry / PurchaseOrderItem), with supplier_id data isolation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalInquiryService {

    // 注意：以下 Mapper 来自 atlas-purchase，此处为示意；实际开发需添加依赖或使用 Feign 调用 /
    // Note: The following Mappers are from atlas-purchase; this is illustrative; in practice add dependency or use Feign
    // private final InquiryMapper inquiryMapper;
    // private final QuotationMapper quotationMapper;
    // private final InquiryMaterialMapper inquiryMaterialMapper;

    /**
     * Redis 模板（竞价实时数据存储）— 若 Redis 未配置则降级为内存级模拟 /
     * Redis template (bidding real-time data storage) — falls back to in-memory simulation if Redis not configured
     */
    private final StringRedisTemplate stringRedisTemplate;

    /** Redis Key 前缀 / Redis key prefix */
    private static final String BIDDING_BIDS_KEY = "bidding:%d:bids";
    private static final String BIDDING_DEADLINE_KEY = "bidding:%d:deadline";
    private static final String BIDDING_SUPPLIER_BID_KEY = "bidding:%d:supplier:%d:bid";

    /** 竞价截止时间格式化 / Bidding deadline formatter */
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查看企业发来的询价单列表（分页、状态筛选） /
     * View inquiry list from enterprises (paginated, with status filter)
     *
     * @param page   页码 / Page number
     * @param size   每页条数 / Page size
     * @param status 状态筛选（可选） / Status filter (optional)
     * @return 分页结果 / Paginated result
     */
    public Page<InquiryViewResponse> listInquiries(int page, int size, String status) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // TODO: 实际实现需查询 atlas-purchase 的 Inquiry 表 /
        // TODO: Actual implementation queries Inquiry table from atlas-purchase
        // LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(Inquiry::getTargetSupplierId, supplierId);
        // if (StringUtils.hasText(status)) {
        //     wrapper.eq(Inquiry::getStatus, status);
        // }
        // wrapper.orderByDesc(Inquiry::getPublishedAt);

        log.info("查询询价单列表: supplierId={}, status={}", supplierId, status);
        // return inquiryMapper.selectPage(new Page<>(page, size), wrapper);
        // 暂返回空结果 / Temporarily return empty result
        return new Page<>(page, size);
    }

    /**
     * 查看询价单详情（物料清单、要求交期、其他报价人匿名列表） /
     * View inquiry detail (material list, required delivery date, anonymous list of other bidders)
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 询价单详情 / Inquiry detail
     */
    public InquiryViewResponse getInquiryDetail(Long inquiryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // TODO: 实现询价单详情查询，包含物料清单和匿名报价人列表 /
        // TODO: Implement inquiry detail query with material list and anonymous bidder list

        log.info("查看询价单详情: inquiryId={}, supplierId={}", inquiryId, supplierId);
        // InquiryViewResponse response = buildInquiryResponse(inquiryId);
        // 验证供应商权限 / Validate supplier permission
        // if (!response.getTargetSupplierIds().contains(supplierId)) {
        //     throw new BizException(ErrorCode.FORBIDDEN, "无权查看该询价单 / Not authorized to view this inquiry");
        // }
        // return response;
        throw new BizException(ErrorCode.NOT_FOUND,
                "询价单功能开发中，请参考 atlas-purchase 模块实现 / Inquiry feature under development, refer to atlas-purchase module");
    }

    /**
     * 提交报价（含物料单价、总价、交期、有效期） /
     * Submit quotation (unit price, total, delivery date, validity period)
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @param request   报价请求 / Quotation request
     */
    public void submitQuote(Long inquiryId, QuotationSubmitRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 校验报价截止时间 / Validate quotation deadline
        // Inquiry inquiry = inquiryMapper.selectById(inquiryId);
        // if (inquiry.getQuotationDeadline().isBefore(LocalDateTime.now())) {
        //     throw new BizException(ErrorCode.BAD_REQUEST, "报价已截止 / Quotation deadline has passed");
        // }

        // 校验物料清单完整性 / Validate material list completeness
        validateQuotationItems(inquiryId, request);

        // 保存报价 / Save quotation
        log.info("供应商提交报价: inquiryId={}, supplierId={}, totalAmount={}",
                inquiryId, supplierId, request.getTotalAmount());
        // Quotation quotation = new Quotation();
        // quotation.setInquiryId(inquiryId);
        // quotation.setSupplierId(supplierId);
        // quotation.setTotalAmount(request.getTotalAmount());
        // quotation.setValidUntil(request.getValidUntil());
        // quotation.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        // quotationMapper.insert(quotation);
    }

    /**
     * 修改报价（未截止前） / Modify quotation (before deadline)
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @param request   修改后的报价 / Modified quotation
     */
    public void updateQuote(Long inquiryId, QuotationSubmitRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 校验是否已截止 / Check if deadline has passed
        // Quotation quotation = quotationMapper.selectOne(
        //     new LambdaQueryWrapper<Quotation>()
        //         .eq(Quotation::getInquiryId, inquiryId)
        //         .eq(Quotation::getSupplierId, supplierId)
        // );
        // if (quotation == null) {
        //     throw new BizException(ErrorCode.NOT_FOUND, "未找到报价记录 / Quotation not found");
        // }

        log.info("供应商修改报价: inquiryId={}, supplierId={}", inquiryId, supplierId);
        // quotation.setTotalAmount(request.getTotalAmount());
        // quotation.setValidUntil(request.getValidUntil());
        // quotation.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        // quotationMapper.updateById(quotation);
    }

    /**
     * 查看中标结果 / View award result
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 中标结果 / Award result
     */
    public Map<String, Object> getAwardResult(Long inquiryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        log.info("查看中标结果: inquiryId={}, supplierId={}", inquiryId, supplierId);
        // Inquiry inquiry = inquiryMapper.selectById(inquiryId);
        // 检查是否已定标 / Check if award has been made
        // if (!"AWARDED".equals(inquiry.getStatus())) {
        //     return Map.of("status", inquiry.getStatus(), "message", "尚未定标 / Not yet awarded");
        // }
        // 返回中标结果 / Return award result
        Map<String, Object> result = new HashMap<>();
        result.put("inquiryId", inquiryId);
        result.put("status", "PENDING_AWARD");
        result.put("message", "定标结果尚未发布 / Award result not yet published");
        return result;
    }

    /**
     * 报价历史 / Quotation history
     *
     * @param page 页码 / Page number
     * @param size 每页条数 / Page size
     * @return 分页历史记录 / Paginated history
     */
    public Page<Map<String, Object>> getQuotationHistory(int page, int size) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        log.info("查询报价历史: supplierId={}", supplierId);
        // 查询该供应商所有历史报价 / Query all historical quotations for this supplier
        // Page<Quotation> quotationPage = quotationMapper.selectPage(
        //     new Page<>(page, size),
        //     new LambdaQueryWrapper<Quotation>()
        //         .eq(Quotation::getSupplierId, supplierId)
        //         .orderByDesc(Quotation::getCreatedAt)
        // );
        return new Page<>(page, size);
    }

    // ==================== 竞价大厅 / Bidding Room ====================

    /**
     * 获取竞标大厅实时信息 — 包含当前排名（匿名）、我的报价、剩余时间、最低价 /
     * Get bidding room real-time info — current ranking (anonymous), my bid, remaining time, lowest price
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 竞标大厅视图 / Bidding room view
     */
    public BiddingRoomResponse getBiddingRoomInfo(Long inquiryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        String bidsKey = String.format(BIDDING_BIDS_KEY, inquiryId);
        String deadlineKey = String.format(BIDDING_DEADLINE_KEY, inquiryId);

        BiddingRoomResponse response = new BiddingRoomResponse();
        response.setInquiryId(inquiryId);
        // TODO: 从数据库查询询价单基本信息（标题、编号）/ Query inquiry basic info from DB
        response.setInquiryTitle("询价单 #" + inquiryId + " / Inquiry #" + inquiryId);
        response.setInquiryNo("INQ-" + inquiryId);

        // 获取截止时间 / Get deadline
        String deadlineStr = stringRedisTemplate.opsForValue().get(deadlineKey);
        if (deadlineStr != null) {
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr, DEADLINE_FORMATTER);
            response.setDeadline(deadline);
            long remainingSeconds = Duration.between(LocalDateTime.now(), deadline).getSeconds();
            response.setRemainingSeconds(Math.max(0, remainingSeconds));
            response.setStatus(remainingSeconds > 0 ? "OPEN" : "CLOSED");
        } else {
            // 无截止时间时默认 OPEN / Default OPEN when no deadline set
            response.setStatus("OPEN");
            response.setRemainingSeconds(0L);
        }

        // 获取当前最低价 / Get current lowest price
        Set<String> topBid = stringRedisTemplate.opsForZSet().rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 1);
        if (topBid != null && !topBid.isEmpty()) {
            // ZSet 中 member 为 "supplierId:price" / Member format is "supplierId:price"
            String[] parts = topBid.iterator().next().split(":");
            response.setCurrentLowestPrice(new BigDecimal(parts[1]));
        }

        // 获取参与供应商数 / Get participating supplier count
        Long bidderCount = stringRedisTemplate.opsForZSet().zCard(bidsKey);
        response.setBidderCount(bidderCount != null ? bidderCount.intValue() : 0);

        // 获取总出价次数 / Get total bid count
        Long totalBids = stringRedisTemplate.opsForZSet().size(bidsKey);
        response.setTotalBids(totalBids != null ? totalBids.intValue() : 0);

        // 获取我的排名和报价 / Get my rank and last bid
        String supplierBidKey = String.format(BIDDING_SUPPLIER_BID_KEY, inquiryId, supplierId);
        String myLastBidStr = stringRedisTemplate.opsForValue().get(supplierBidKey);
        if (myLastBidStr != null) {
            response.setMyLastBid(new BigDecimal(myLastBidStr));

            // 获取排名: ZRANK 返回 0-based → 转换为 1-based /
            // Get rank: ZRANK returns 0-based → convert to 1-based
            Long reverseRank = stringRedisTemplate.opsForZSet().reverseRank(bidsKey,
                    supplierId + ":" + myLastBidStr);
            if (reverseRank != null) {
                response.setMyRank((int) (reverseRank + 1));
            }
        }

        // 构建匿名排名快照 / Build anonymous ranking snapshot
        Set<String> allBids = stringRedisTemplate.opsForZSet()
                .rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 20);
        if (allBids != null && !allBids.isEmpty()) {
            List<BiddingRoomResponse.RankingSnapshot> snapshots = new ArrayList<>();
            int rank = 0;
            BigDecimal previousPrice = null;
            for (String bidStr : allBids) {
                rank++;
                String[] parts = bidStr.split(":");
                BigDecimal price = new BigDecimal(parts[1]);
                BiddingRoomResponse.RankingSnapshot snapshot = new BiddingRoomResponse.RankingSnapshot();
                snapshot.setRank(rank);
                snapshot.setPrice(price);
                // 与上一名价差 / Gap from previous rank
                if (previousPrice != null) {
                    snapshot.setGapFromPrevious(price.subtract(previousPrice));
                } else {
                    snapshot.setGapFromPrevious(BigDecimal.ZERO);
                }
                previousPrice = price;
                snapshots.add(snapshot);
            }
            response.setRankingSnapshots(snapshots);
        }

        log.info("获取竞标大厅信息: inquiryId={}, supplierId={}, bidderCount={}, myRank={}",
                inquiryId, supplierId, response.getBidderCount(), response.getMyRank());
        return response;
    }

    /**
     * 提交竞价 — 校验报价低于最低价、在截止时间内、自动更新排名 /
     * Submit bid — validate price lower than current lowest, within deadline, auto-update ranking
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @param request   竞价请求 / Bid request
     */
    public void submitBid(Long inquiryId, BidRequest request) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        String bidsKey = String.format(BIDDING_BIDS_KEY, inquiryId);
        String deadlineKey = String.format(BIDDING_DEADLINE_KEY, inquiryId);
        String supplierBidKey = String.format(BIDDING_SUPPLIER_BID_KEY, inquiryId, supplierId);

        // 1. 校验竞价截止时间 / Validate bidding deadline
        String deadlineStr = stringRedisTemplate.opsForValue().get(deadlineKey);
        if (deadlineStr != null) {
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr, DEADLINE_FORMATTER);
            if (LocalDateTime.now().isAfter(deadline)) {
                throw new BizException(ErrorCode.BAD_REQUEST,
                        "竞价已截止，无法出价 / Bidding has closed, cannot submit bid");
            }
        }

        // 2. 校验报价低于当前最低价（首次报价无限制）/ Validate price lower than current lowest (no limit for first bid)
        Set<String> currentLowest = stringRedisTemplate.opsForZSet()
                .rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 1);
        if (currentLowest != null && !currentLowest.isEmpty()) {
            String[] parts = currentLowest.iterator().next().split(":");
            BigDecimal currentLowestPrice = new BigDecimal(parts[1]);
            Long lowestSupplierId = Long.parseLong(parts[0]);

            // 如果最低价是自己出的，允许再次出价（但必须更低）/ If lowest is self, allow re-bid (but must be lower)
            if (request.getTotalPrice().compareTo(currentLowestPrice) >= 0) {
                throw new BizException(ErrorCode.BAD_REQUEST,
                        String.format("报价必须低于当前最低价 %s / Bid must be lower than current lowest price %s",
                                currentLowestPrice, currentLowestPrice));
            }
        }

        // 3. 更新 Sorted Set: ZADD bidding:{id}:bids price "supplierId:price" /
        //    Update Sorted Set: ZADD bidding:{id}:bids price "supplierId:price"
        String member = supplierId + ":" + request.getTotalPrice().toPlainString();
        stringRedisTemplate.opsForZSet().add(bidsKey, member, request.getTotalPrice().doubleValue());

        // 4. 记录当前供应商的出价 / Record current supplier's bid
        stringRedisTemplate.opsForValue().set(supplierBidKey, request.getTotalPrice().toPlainString());

        // 5. 模拟 WebSocket 推送排名变化 / Simulate WebSocket push for ranking change
        log.info("WebSocket推送: 竞价排名更新, inquiryId={}, supplierId={}, newPrice={}",
                inquiryId, supplierId, request.getTotalPrice());

        // 6. 记录出价历史到 MySQL（由定时任务或 AOP 异步写入） /
        //    Record bid history to MySQL (async write by scheduled task or AOP)
        log.info("供应商提交竞价: inquiryId={}, supplierId={}, totalPrice={}, deliveryDays={}, validUntil={}",
                inquiryId, supplierId, request.getTotalPrice(),
                request.getDeliveryDays(), request.getValidUntil());
    }

    /**
     * 获取竞价出价历史 — 匿名化展示价格变化曲线，仅显示价格和排名变化 /
     * Get bid history — anonymized price change curve, only shows price and rank changes
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 竞价历史 / Bid history
     */
    public BidHistoryResponse getBidHistory(Long inquiryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        String bidsKey = String.format(BIDDING_BIDS_KEY, inquiryId);

        BidHistoryResponse response = new BidHistoryResponse();
        response.setInquiryId(inquiryId);
        // TODO: 从数据库查询询价单编号 / Query inquiry number from DB
        response.setInquiryNo("INQ-" + inquiryId);

        // 获取所有出价记录（按价格升序） / Get all bids (ascending by price)
        Set<String> allBids = stringRedisTemplate.opsForZSet()
                .rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 100);

        if (allBids == null || allBids.isEmpty()) {
            response.setTotalBids(0);
            response.setBidderCount(0);
            response.setRecords(new ArrayList<>());
            return response;
        }

        // 参与供应商数 / Participating supplier count
        Long bidderCount = stringRedisTemplate.opsForZSet().zCard(bidsKey);
        response.setBidderCount(bidderCount != null ? bidderCount.intValue() : 0);
        response.setTotalBids(allBids.size());

        // 起始最低价和当前最低价 / Starting lowest price and current lowest price
        String[] firstParts = allBids.iterator().next().split(":");
        BigDecimal firstPrice = new BigDecimal(firstParts[1]);

        // 获取当前最低价 / Get current lowest price
        Set<String> lowestNow = stringRedisTemplate.opsForZSet()
                .rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 1);
        BigDecimal currentLowest = firstPrice;
        if (lowestNow != null && !lowestNow.isEmpty()) {
            String[] lowestParts = lowestNow.iterator().next().split(":");
            currentLowest = new BigDecimal(lowestParts[1]);
        }

        response.setStartingLowestPrice(firstPrice);
        response.setCurrentLowestPrice(currentLowest);
        response.setPriceReduction(firstPrice.subtract(currentLowest));

        // 构建出价记录 / Build bid records
        List<BidHistoryResponse.BidRecord> records = new ArrayList<>();
        int rank = 0;
        for (String bidStr : allBids) {
            rank++;
            String[] parts = bidStr.split(":");
            Long bidSupplierId = Long.parseLong(parts[0]);
            BigDecimal price = new BigDecimal(parts[1]);

            BidHistoryResponse.BidRecord record = new BidHistoryResponse.BidRecord();
            record.setBidTime(LocalDateTime.now()); // 精确时间需从历史表获取 / Accurate time needs history table
            record.setPrice(price);
            record.setRankAfterBid(rank);
            record.setIsMyBid(bidSupplierId.equals(supplierId));
            record.setIsLowest(rank == 1);
            records.add(record);
        }
        response.setRecords(records);

        log.info("查询竞价历史: inquiryId={}, supplierId={}, totalBids={}, bidderCount={}",
                inquiryId, supplierId, response.getTotalBids(), response.getBidderCount());
        return response;
    }

    /**
     * 竞价结束 — 锁定最终排名，标记询价单为已截止状态 /
     * Close bidding — lock final ranking, mark inquiry as closed
     *
     * @param inquiryId 询价单ID / Inquiry ID
     */
    public void closeBidding(Long inquiryId) {
        String deadlineKey = String.format(BIDDING_DEADLINE_KEY, inquiryId);

        // 将截止时间设为当前时间，立即结束 / Set deadline to now, close immediately
        String nowStr = LocalDateTime.now().format(DEADLINE_FORMATTER);
        stringRedisTemplate.opsForValue().set(deadlineKey, nowStr);

        // TODO: 更新数据库询价单状态为 CLOSED / Update inquiry status to CLOSED in database

        log.info("竞价结束: inquiryId={}, closedAt={}", inquiryId, nowStr);
    }

    /**
     * 获取比价结果 — 揭标后供应商可查看各供应商报价对比（供应商名匿名化） /
     * Get comparison result — after opening, suppliers can view bid comparison (supplier names anonymized)
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 比价结果 / Comparison result
     */
    public ComparisonResultResponse getComparisonResult(Long inquiryId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();
        String bidsKey = String.format(BIDDING_BIDS_KEY, inquiryId);

        ComparisonResultResponse response = new ComparisonResultResponse();
        response.setInquiryId(inquiryId);
        // TODO: 从数据库查询询价单基本信息 / Query inquiry basic info from DB
        response.setInquiryNo("INQ-" + inquiryId);
        response.setInquiryTitle("询价单 #" + inquiryId + " / Inquiry #" + inquiryId);

        // 检查是否已揭标 / Check if bids have been opened
        String deadlineStr = stringRedisTemplate.opsForValue()
                .get(String.format(BIDDING_DEADLINE_KEY, inquiryId));
        if (deadlineStr != null) {
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr, DEADLINE_FORMATTER);
            if (LocalDateTime.now().isBefore(deadline)) {
                response.setAwardStatus("PENDING");
                response.setOpenedAt(null);
                response.setTotalSuppliers(0);
                response.setRankings(new ArrayList<>());
                response.setAwardedSupplierCode(null);
                return response;
            }
        }

        response.setAwardStatus("AWARDED");
        response.setOpenedAt(LocalDateTime.now());

        // 获取所有出价排名 / Get all bid rankings
        Set<String> allBids = stringRedisTemplate.opsForZSet()
                .rangeByScore(bidsKey, 0, Double.MAX_VALUE, 0, 50);

        if (allBids == null || allBids.isEmpty()) {
            response.setTotalSuppliers(0);
            response.setRankings(new ArrayList<>());
            return response;
        }

        response.setTotalSuppliers(allBids.size());

        // 构建匿名排名列表 / Build anonymous ranking list
        List<ComparisonResultResponse.SupplierRanking> rankings = new ArrayList<>();
        int rank = 0;
        BigDecimal lowestPrice = null;
        String awardedCode = null;

        for (String bidStr : allBids) {
            rank++;
            String[] parts = bidStr.split(":");
            Long bidSupplierId = Long.parseLong(parts[0]);
            BigDecimal price = new BigDecimal(parts[1]);

            if (lowestPrice == null) {
                lowestPrice = price;
                awardedCode = "SUP-" + (1000 + bidSupplierId + (rank * 7)) % 10000; // 匿名代号 / Anonymous code
            }

            ComparisonResultResponse.SupplierRanking ranking =
                    new ComparisonResultResponse.SupplierRanking();
            ranking.setRank(rank);
            ranking.setSupplierCode("SUP-" + (1000 + bidSupplierId + (rank * 7)) % 10000);
            ranking.setTotalPrice(price);
            ranking.setDeliveryDays(20); // TODO: 从出价明细获取 / Get from bid details
            ranking.setBidTime(LocalDateTime.now());
            ranking.setGapFromLowest(price.subtract(lowestPrice));
            ranking.setIsCurrentSupplier(bidSupplierId.equals(supplierId));
            ranking.setIsAwarded(rank == 1);
            rankings.add(ranking);
        }

        response.setRankings(rankings);
        response.setAwardedSupplierCode(awardedCode);

        log.info("查看比价结果: inquiryId={}, supplierId={}, totalSuppliers={}, awardedCode={}",
                inquiryId, supplierId, response.getTotalSuppliers(), awardedCode);
        return response;
    }

    // ==================== 内部辅助方法 / Internal Helper ====================

    /**
     * 校验报价物料清单是否与询价单匹配 / Validate quotation items match inquiry material list
     */
    private void validateQuotationItems(Long inquiryId, QuotationSubmitRequest request) {
        // TODO: 校验每个物料项都在询价单中存在，且数量一致 /
        // TODO: Validate each item exists in inquiry and quantity matches
    }
}
