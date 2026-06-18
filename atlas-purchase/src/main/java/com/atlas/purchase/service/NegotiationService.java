package com.atlas.purchase.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.NegotiationRound;
import com.atlas.purchase.entity.NegotiationSession;
import com.atlas.purchase.mapper.NegotiationRoundMapper;
import com.atlas.purchase.mapper.NegotiationSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 竞争性谈判业务服务 / Competitive negotiation business service
 *
 * <p>完整生命周期：创建→发布→多轮谈判→收集最终报价→最低价评审→定标。 /
 * Full lifecycle: create → publish → multi-round negotiation → collect final offers → lowest price evaluation → award.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationService {

    private final NegotiationSessionMapper sessionMapper;
    private final NegotiationRoundMapper roundMapper;

    /** 状态常量 / Status constants */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_NEGOTIATING = 2;
    public static final int STATUS_OFFER_COLLECTED = 3;
    public static final int STATUS_REVIEWING = 4;
    public static final int STATUS_AWARDED = 5;
    public static final int STATUS_TERMINATED = 6;

    /**
     * 创建谈判会话（草稿状态） / Create negotiation session (draft status)
     *
     * @param session 谈判会话信息 / Negotiation session info
     * @return 创建成功的会话 / Created session
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationSession createSession(NegotiationSession session) {
        session.setNegotiationNo(generateNo("NT"));
        session.setStatus(STATUS_DRAFT);
        session.setRoundCount(0);
        sessionMapper.insert(session);
        log.info("创建竞争性谈判会话: no={} orderId={}", session.getNegotiationNo(), session.getPurchaseOrderId());
        return session;
    }

    /**
     * 发布谈判（通知邀请供应商） / Publish negotiation (notify invited suppliers)
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationSession publish(Long sessionId) {
        NegotiationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许发布");
        }
        session.setStatus(STATUS_PUBLISHED);
        sessionMapper.updateById(session);
        log.info("谈判已发布: no={}", session.getNegotiationNo());
        return session;
    }

    /**
     * 开始谈判（进入多轮谈判阶段） / Start negotiation (enter multi-round phase)
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationSession startNegotiation(Long sessionId) {
        NegotiationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_PUBLISHED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许开始谈判");
        }
        session.setStatus(STATUS_NEGOTIATING);
        sessionMapper.updateById(session);
        log.info("进入谈判阶段: no={}", session.getNegotiationNo());
        return session;
    }

    /**
     * 提交一轮谈判报价 / Submit a round of negotiation offer
     *
     * @param round 报价记录（含 negotiationId, roundNo, supplierId, offerAmount 等） /
     *              Offer record (including negotiationId, roundNo, supplierId, offerAmount etc.)
     * @return 报价记录 / Offer record
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationRound submitOffer(NegotiationRound round) {
        NegotiationSession session = getSession(round.getNegotiationId());
        if (session.getStatus() != STATUS_NEGOTIATING && session.getStatus() != STATUS_OFFER_COLLECTED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许提交报价");
        }
        roundMapper.insert(round);

        // 更新轮次计数 / Update round count
        session.setRoundCount(session.getRoundCount() + 1);
        if (round.getIsFinal() != null && round.getIsFinal() == 1) {
            session.setStatus(STATUS_OFFER_COLLECTED);
        }
        sessionMapper.updateById(session);

        log.info("报价提交: sessionId={} roundNo={} supplierId={} amount={}",
                round.getNegotiationId(), round.getRoundNo(), round.getSupplierId(), round.getOfferAmount());
        return round;
    }

    /**
     * 最低价评审并定标 / Lowest price evaluation and award
     *
     * <p>从最终报价中选出最低价供应商作为成交方。 / Select the lowest-price supplier from final offers as the winner.</p>
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationSession award(Long sessionId) {
        NegotiationSession session = getSession(sessionId);
        if (session.getStatus() != STATUS_OFFER_COLLECTED && session.getStatus() != STATUS_REVIEWING) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许定标");
        }

        session.setStatus(STATUS_REVIEWING);
        sessionMapper.updateById(session);

        // 查询所有最终报价，取最低价 / Query all final offers, take the lowest
        List<NegotiationRound> finalRounds = roundMapper.selectList(
                new LambdaQueryWrapper<NegotiationRound>()
                        .eq(NegotiationRound::getNegotiationId, sessionId)
                        .eq(NegotiationRound::getIsFinal, 1));

        if (finalRounds.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "未找到最终报价记录");
        }

        NegotiationRound winner = finalRounds.stream()
                .min(Comparator.comparing(NegotiationRound::getOfferAmount))
                .orElseThrow(() -> new BizException(ErrorCode.BAD_REQUEST.getCode(), "无法确定成交供应商"));

        session.setWinnerSupplierId(winner.getSupplierId());
        session.setWinnerAmount(winner.getOfferAmount());
        session.setStatus(STATUS_AWARDED);
        sessionMapper.updateById(session);

        log.info("定标完成: no={} winnerSupplierId={} amount={}",
                session.getNegotiationNo(), winner.getSupplierId(), winner.getOfferAmount());
        return session;
    }

    /**
     * 终止谈判 / Terminate negotiation
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 更新后的会话 / Updated session
     */
    @Transactional(rollbackFor = Exception.class)
    public NegotiationSession terminate(Long sessionId) {
        NegotiationSession session = getSession(sessionId);
        if (session.getStatus() == STATUS_AWARDED || session.getStatus() == STATUS_TERMINATED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许终止");
        }
        session.setStatus(STATUS_TERMINATED);
        sessionMapper.updateById(session);
        log.info("谈判已终止: no={}", session.getNegotiationNo());
        return session;
    }

    /**
     * 分页查询谈判会话（支持 keyword + status 筛选） /
     * Paginated query of negotiation sessions (supports keyword + status filtering)
     *
     * @param keyword 关键字（匹配谈判编号/标题） / Keyword (matches negotiation number/title)
     * @param status  状态（可选） / Status (optional)
     * @param page    当前页 / Current page
     * @param size    每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<NegotiationSession> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<NegotiationSession> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(NegotiationSession::getNegotiationNo, keyword)
                    .or().like(NegotiationSession::getTitle, keyword));
        }
        if (status != null) {
            wrapper.eq(NegotiationSession::getStatus, status);
        }
        wrapper.orderByDesc(NegotiationSession::getCreatedAt);
        return sessionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询谈判会话详情 / Query negotiation session detail
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 谈判会话 / Negotiation session
     */
    public NegotiationSession getSession(Long sessionId) {
        NegotiationSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST, "谈判会话不存在");
        }
        return session;
    }

    /**
     * 查询谈判报价记录列表 / Query negotiation offer record list
     *
     * @param sessionId 谈判会话ID / Negotiation session ID
     * @return 报价记录列表 / Offer record list
     */
    public List<NegotiationRound> listRounds(Long sessionId) {
        return roundMapper.selectList(
                new LambdaQueryWrapper<NegotiationRound>()
                        .eq(NegotiationRound::getNegotiationId, sessionId)
                        .orderByAsc(NegotiationRound::getRoundNo));
    }

    /** 生成编号：NT + 日期 + 随机串 / Generate number: NT + date + random string */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
