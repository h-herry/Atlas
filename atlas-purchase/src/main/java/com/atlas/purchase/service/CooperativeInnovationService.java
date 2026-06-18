package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.CooperativeInnovation;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.CooperativeInnovationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 合作创新采购业务服务 / Cooperative innovation procurement service
 *
 * <p>状态流转：COLLECTING(0) → REVIEWING(1) → COOPERATING(2) → ACCEPTING(3) → COMPLETED(4) /
 * Status flow: COLLECTING(0) → REVIEWING(1) → COOPERATING(2) → ACCEPTING(3) → COMPLETED(4)
 * <br>任意非终态可跳转到 TERMINATED(5)。 / Any non-terminal state can jump to TERMINATED(5).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativeInnovationService {

    // 状态常量 / Status constants
    private static final int STATUS_COLLECTING = 0;    // 征集中 / Collecting
    private static final int STATUS_REVIEWING = 1;     // 评审中 / Reviewing
    private static final int STATUS_COOPERATING = 2;   // 合作中 / Cooperating
    private static final int STATUS_ACCEPTING = 3;     // 验收中 / Accepting
    private static final int STATUS_COMPLETED = 4;     // 已完成 / Completed
    private static final int STATUS_TERMINATED = 5;    // 已终止 / Terminated

    private final CooperativeInnovationMapper cooperativeInnovationMapper;

    // ==================== 生命周期 / Lifecycle ====================

    /**
     * 从采购订单创建合作创新 / Create cooperative innovation from purchase order
     */
    @Transactional(rollbackFor = Exception.class)
    public CooperativeInnovation createFromOrder(PurchaseOrder order) {
        CooperativeInnovation innovation = new CooperativeInnovation();
        innovation.setInnovationNo(generateNo("CX"));
        innovation.setPurchaseOrderId(order.getId());
        innovation.setTitle(order.getTitle());
        innovation.setIpOwnership("SHARED");
        innovation.setStageCount(3);
        innovation.setStatus(STATUS_COLLECTING);
        cooperativeInnovationMapper.insert(innovation);
        log.info("创建合作创新采购: innovationNo={}", innovation.getInnovationNo());
        return innovation;
    }

    /**
     * 进入评审 — 审查研发方案 / Enter review — evaluate R&D proposal
     */
    @Transactional(rollbackFor = Exception.class)
    public void startReview(Long id, String rdContent, String rdCycle,
                             java.math.BigDecimal rdBudget, Long partnerSupplierId) {
        CooperativeInnovation innovation = getById(id);
        if (innovation.getStatus() != STATUS_COLLECTING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅征集中状态可进入评审 / Only COLLECTING status can enter review");
        }
        innovation.setRdContent(rdContent);
        innovation.setRdCycle(rdCycle);
        innovation.setRdBudget(rdBudget);
        innovation.setPartnerSupplierId(partnerSupplierId);
        innovation.setStatus(STATUS_REVIEWING);
        cooperativeInnovationMapper.updateById(innovation);
        log.info("进入评审: innovationNo={}", innovation.getInnovationNo());
    }

    /**
     * 开始合作 — 确定合作关系 / Start cooperation — confirm partnership
     */
    @Transactional(rollbackFor = Exception.class)
    public void startCooperating(Long id) {
        CooperativeInnovation innovation = getById(id);
        if (innovation.getStatus() != STATUS_REVIEWING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅评审中状态可开始合作 / Only REVIEWING status can start cooperation");
        }
        innovation.setStatus(STATUS_COOPERATING);
        cooperativeInnovationMapper.updateById(innovation);
        log.info("开始合作: innovationNo={}", innovation.getInnovationNo());
    }

    /**
     * 进入验收 — 逐阶段验收 / Enter acceptance — staged acceptance
     */
    @Transactional(rollbackFor = Exception.class)
    public void startAcceptance(Long id, String stageProgress) {
        CooperativeInnovation innovation = getById(id);
        if (innovation.getStatus() != STATUS_COOPERATING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅合作中状态可进入验收 / Only COOPERATING status can enter acceptance");
        }
        innovation.setStageProgress(stageProgress);
        innovation.setStatus(STATUS_ACCEPTING);
        cooperativeInnovationMapper.updateById(innovation);
        log.info("进入验收: innovationNo={}, 进度={}", innovation.getInnovationNo(), stageProgress);
    }

    /**
     * 完成合作 / Complete cooperation
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        CooperativeInnovation innovation = getById(id);
        if (innovation.getStatus() != STATUS_ACCEPTING) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "仅验收中状态可完成 / Only ACCEPTING status can complete");
        }
        innovation.setStatus(STATUS_COMPLETED);
        cooperativeInnovationMapper.updateById(innovation);
        log.info("合作创新采购完成: innovationNo={}", innovation.getInnovationNo());
    }

    /**
     * 终止合作 / Terminate cooperation
     */
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long id) {
        CooperativeInnovation innovation = getById(id);
        if (innovation.getStatus() == STATUS_COMPLETED) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "已完成不可终止 / Completed cannot be terminated");
        }
        innovation.setStatus(STATUS_TERMINATED);
        cooperativeInnovationMapper.updateById(innovation);
        log.info("终止合作创新采购: innovationNo={}", innovation.getInnovationNo());
    }

    // ==================== 查询 / Query ====================

    /**
     * 查询合作创新详情 / Query cooperative innovation detail
     */
    public CooperativeInnovation getById(Long id) {
        CooperativeInnovation innovation = cooperativeInnovationMapper.selectById(id);
        if (innovation == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "合作创新采购记录不存在: " + id);
        }
        return innovation;
    }

    /**
     * 分页查询合作创新列表 / Paginated query of cooperative innovations
     */
    public IPage<CooperativeInnovation> page(IPage<CooperativeInnovation> page, String keyword, Integer status) {
        LambdaQueryWrapper<CooperativeInnovation> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(CooperativeInnovation::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(CooperativeInnovation::getStatus, status);
        }
        wrapper.orderByDesc(CooperativeInnovation::getCreatedAt);
        return cooperativeInnovationMapper.selectPage(page, wrapper);
    }

    /**
     * 生成编号 / Generate serial number
     */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
