package com.atlas.purchase.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.ConsultationReview;
import com.atlas.purchase.entity.ConsultationSession;
import com.atlas.purchase.mapper.ConsultationReviewMapper;
import com.atlas.purchase.mapper.ConsultationSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 竞争性磋商业务服务 / Competitive consultation business service
 *
 * <p>完整生命周期：创建→公告期→响应收集→磋商→最终报价→综合评分排名→定标。 /
 * Full lifecycle: Create → Announce → Response Collection → Consultation → Final Offer → Comprehensive Scoring → Award.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationSessionMapper sessionMapper;
    private final ConsultationReviewMapper reviewMapper;

    // 状态常量 / Status constants
    public static final int STATUS_DRAFT = 0;                // 草稿 / Draft
    public static final int STATUS_ANNOUNCEMENT = 1;         // 公告期 / Announcement
    public static final int STATUS_RESPONSE = 2;              // 响应收集 / Response collection
    public static final int STATUS_CONSULTING = 3;            // 磋商中 / Consulting
    public static final int STATUS_FINAL_OFFER = 4;           // 最终报价 / Final offer
    public static final int STATUS_COMPREHENSIVE_REVIEW = 5;  // 综合评审 / Comprehensive review
    public static final int STATUS_AWARDED = 6;               // 已定标 / Awarded
    public static final int STATUS_TERMINATED = 7;            // 已终止 / Terminated

    /**
     * 创建磋商会话（草稿状态） / Create consultation session (draft)
     *
     * @param session 磋商会话信息 / Consultation session info
     * @return 创建成功的会话 / Created session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession createSession(ConsultationSession session) {
        session.setConsultationNo(generateNo("CS"));
        session.setStatus(STATUS_DRAFT);
        sessionMapper.insert(session);
        log.info("创建竞争性磋商会话: no={} orderId={}", session.getConsultationNo(), session.getPurchaseOrderId());
        return session;
    }

    /**
     * 进入公告期 / Enter announcement period
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession announce(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许进入公告期 / Current status does not allow announcement");
        }
        session.setStatus(STATUS_ANNOUNCEMENT);
        sessionMapper.updateById(session);
        log.info("磋商进入公告期: no={}", session.getConsultationNo());
        return session;
    }

    /**
     * 开启响应文件提交 / Open response document submission
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession openResponse(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_ANNOUNCEMENT) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许开启响应提交 / Status does not allow response submission");
        }
        session.setStatus(STATUS_RESPONSE);
        sessionMapper.updateById(session);
        log.info("响应提交已开启: no={}", session.getConsultationNo());
        return session;
    }

    /**
     * 进入磋商阶段 / Enter consultation phase
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession startConsultation(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_RESPONSE) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许进入磋商 / Status does not allow consultation");
        }
        session.setStatus(STATUS_CONSULTING);
        sessionMapper.updateById(session);
        log.info("进入磋商阶段: no={}", session.getConsultationNo());
        return session;
    }

    /**
     * 触发最终报价 / Trigger final offer
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession finalOffer(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_CONSULTING) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许触发最终报价 / Status does not allow final offer");
        }
        session.setStatus(STATUS_FINAL_OFFER);
        sessionMapper.updateById(session);
        log.info("触发最终报价: no={}", session.getConsultationNo());
        return session;
    }

    /**
     * 提交评审记录（技术+商务+价格综合评分） / Submit review record (technical + business + price composite scoring)
     *
     * @param review 评审记录 / Review record
     * @return 评审记录 / Review record
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationReview submitReview(ConsultationReview review) {
        ConsultationSession session = getSession(review.getConsultationId());
        if (session.getStatus() != STATUS_FINAL_OFFER && session.getStatus() != STATUS_COMPREHENSIVE_REVIEW) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许提交评审 / Status does not allow review submission");
        }
        session.setStatus(STATUS_COMPREHENSIVE_REVIEW);
        sessionMapper.updateById(session);

        reviewMapper.insert(review);
        log.info("评审提交: consultationId={} supplierId={} totalScore={}",
                review.getConsultationId(), review.getSupplierId(), review.getTotalScore());
        return review;
    }

    /**
     * 综合评审定标：按综合得分排名，取第一名 / Comprehensive evaluation and award: rank by composite score, pick top
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession award(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_COMPREHENSIVE_REVIEW) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许定标 / Status does not allow award");
        }

        List<ConsultationReview> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<ConsultationReview>()
                        .eq(ConsultationReview::getConsultationId, sessionId));

        if (reviews.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "未找到评审记录 / No review records found");
        }

        // 按综合得分降序排名 / Sort by composite score descending
        List<ConsultationReview> sorted = reviews.stream()
                .sorted(Comparator.comparing(ConsultationReview::getTotalScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 更新排名与中标标记 / Update ranking and winner flag
        for (int i = 0; i < sorted.size(); i++) {
            ConsultationReview review = sorted.get(i);
            review.setRank(i + 1);
            review.setIsWinner(i == 0 ? 1 : 0);
            reviewMapper.updateById(review);
        }

        ConsultationReview winner = sorted.get(0);
        session.setWinnerSupplierId(winner.getSupplierId());
        session.setWinnerAmount(winner.getFinalOffer());
        session.setStatus(STATUS_AWARDED);
        sessionMapper.updateById(session);

        log.info("磋商定标完成: no={} winnerSupplierId={} totalScore={}",
                session.getConsultationNo(), winner.getSupplierId(), winner.getTotalScore());
        return session;
    }

    /**
     * 终止磋商 / Terminate consultation
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession terminate(Long sessionId) {
        ConsultationSession session = getSession(sessionId);
        if (session.getStatus() == STATUS_AWARDED || session.getStatus() == STATUS_TERMINATED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许终止 / Status does not allow termination");
        }
        session.setStatus(STATUS_TERMINATED);
        sessionMapper.updateById(session);
        log.info("磋商已终止: no={}", session.getConsultationNo());
        return session;
    }

    /**
     * 分页查询磋商会话（支持 keyword + status 筛选） / Paginated query (supports keyword + status filtering)
     *
     * @param keyword 关键字 / Keyword
     * @param status  状态（可选） / Status (optional)
     * @param page    当前页 / Current page
     * @param size    每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<ConsultationSession> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ConsultationSession::getConsultationNo, keyword)
                    .or().like(ConsultationSession::getTitle, keyword));
        }
        if (status != null) {
            wrapper.eq(ConsultationSession::getStatus, status);
        }
        wrapper.orderByDesc(ConsultationSession::getCreatedAt);
        return sessionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询磋商会话详情 / Query consultation session detail
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 磋商会话 / Consultation session
     */
    public ConsultationSession getSession(Long sessionId) {
        ConsultationSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST, "磋商会话不存在 / Session not found");
        }
        return session;
    }

    /**
     * 查询磋商评审记录列表 / Query review record list
     *
     * @param sessionId 磋商会话ID / Session ID
     * @return 评审记录列表（按排名升序） / Review list (sorted by rank ascending)
     */
    public List<ConsultationReview> listReviews(Long sessionId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<ConsultationReview>()
                        .eq(ConsultationReview::getConsultationId, sessionId)
                        .orderByAsc(ConsultationReview::getRank));
    }

    /**
     * 生成编号：CS + 日期 + 随机串 / Generate number: CS + date + random string
     */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
