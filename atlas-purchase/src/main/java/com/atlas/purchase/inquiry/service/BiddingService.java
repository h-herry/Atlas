package com.atlas.purchase.inquiry.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.inquiry.entity.BiddingSession;
import com.atlas.purchase.inquiry.mapper.BiddingSessionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 竞价大厅业务服务（Redis SortedSet 实时排名） / Bidding hall service (Redis SortedSet real-time ranking)
 *
 * <p>基于 Redis ZSet 实现公开/密封竞价，支持实时排名查询。 /
 * Implements OPEN/SEALED bidding via Redis ZSet with real-time ranking queries.
 * Redis key: bidding:{sessionId}</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiddingService extends ServiceImpl<BiddingSessionMapper, BiddingSession> {

    private final BiddingSessionMapper sessionMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String BIDDING_KEY_PREFIX = "bidding:";

    /**
     * 创建竞价场次 / Create bidding session
     */
    @Transactional(rollbackFor = Exception.class)
    public BiddingSession createSession(BiddingSession session) {
        session.setStatus("PENDING");
        save(session);
        log.info("竞价场次创建: id={} type={}", session.getId(), session.getBiddingType());
        return session;
    }

    /**
     * 启动竞价 / Start bidding session
     */
    @Transactional(rollbackFor = Exception.class)
    public BiddingSession startSession(Long sessionId) {
        BiddingSession session = getById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"PENDING".equals(session.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅待启动场次可启动 / Only pending sessions can be started");
        }
        session.setStatus("ACTIVE");
        session.setStartTime(LocalDateTime.now());
        updateById(session);

        // 初始化 Redis SortedSet / Initialize Redis SortedSet
        String key = BIDDING_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.info("竞价启动: sessionId={}", sessionId);
        return session;
    }

    /**
     * 供应商出价 / Supplier places bid
     *
     * <p>回调到 Redis SortedSet，按价格升序排名（价格越低排名越前）。 /
     * Push to Redis SortedSet, ranked ascending by price (lower = better).</p>
     */
    public int bid(Long sessionId, Long supplierId, BigDecimal bidAmount) {
        BiddingSession session = getById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new BizException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            throw new BizException(ErrorCode.AUCTION_CLOSED);
        }

        // 底价校验 / Floor price check
        if (session.getFloorPrice() != null && bidAmount.compareTo(session.getFloorPrice()) < 0) {
            throw new BizException(ErrorCode.BID_REJECTED_LOW);
        }

        String key = BIDDING_KEY_PREFIX + sessionId;
        // 供应商标识: supplierId + 纳秒时间戳确保唯一 / Supplier identifier with nanos for uniqueness
        String member = supplierId + "_" + System.nanoTime();
        redisTemplate.opsForZSet().add(key, member, bidAmount.doubleValue());

        // 计算排名：价格升序（低价格 = 低 score = 高排名） / Ascending by price (lower price = lower score = higher rank)
        Long rank = redisTemplate.opsForZSet().rank(key, member);
        int position = rank != null ? rank.intValue() + 1 : -1;

        log.info("竞价出价: sessionId={} supplierId={} amount={} rank={}", sessionId, supplierId, bidAmount, position);

        // 密封竞价不返回排名 / Sealed bidding does not return ranking
        if ("SEALED".equals(session.getBiddingType())) {
            return -1;
        }
        return position;
    }

    /**
     * 获取实时排名（公开竞价） / Get real-time ranking (open bidding)
     *
     * <p>返回按价格升序排列的匿名排名列表。 /
     * Returns anonymized ranking list sorted by price ascending.</p>
     */
    public List<Map<String, Object>> getRanking(Long sessionId) {
        String key = BIDDING_KEY_PREFIX + sessionId;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .rangeWithScores(key, 0, -1);

        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = 1;
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("rank", rank++);
                entry.put("price", t.getScore());
                // 公开竞价展示供应商ID / Show supplier ID in open bidding
                // 密封竞价状态下不展示 / Not displayed in sealed bidding
                BiddingSession session = getById(sessionId);
                if (session != null && "OPEN".equals(session.getBiddingType())) {
                    String member = Objects.requireNonNull(t.getValue());
                    entry.put("supplierId", member.split("_")[0]);
                }
                rankings.add(entry);
            }
        }
        return rankings;
    }

    /**
     * 关闭竞价 / Close bidding session
     *
     * <p>密封竞价结束后揭晓最终排名。 /
     * Reveals final ranking after sealed bidding ends.</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> closeSession(Long sessionId) {
        BiddingSession session = getById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if ("CLOSED".equals(session.getStatus())) {
            throw new BizException(ErrorCode.AUCTION_CLOSED);
        }

        session.setStatus("CLOSED");
        session.setEndTime(LocalDateTime.now());
        updateById(session);

        // 揭晓最终排名 / Reveal final ranking
        String key = BIDDING_KEY_PREFIX + sessionId;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .rangeWithScores(key, 0, -1);

        List<Map<String, Object>> finalRanking = new ArrayList<>();
        int rank = 1;
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("rank", rank++);
                entry.put("supplierId", Objects.requireNonNull(t.getValue()).split("_")[0]);
                entry.put("price", t.getScore());
                finalRanking.add(entry);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "CLOSED");
        result.put("finalRanking", finalRanking);

        log.info("竞价关闭: sessionId={} totalBids={}", sessionId, finalRanking.size());
        return result;
    }
}
