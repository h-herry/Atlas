package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierImprovement;
import com.atlas.supplier.mapper.SupplierImprovementMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商改善跟踪闭环 Service — 创建整改单 / 记录措施 / 验证闭环 /
 * Supplier improvement tracking (closed-loop) Service — create improvement / record action / verify closure
 *
 * <p>整改闭环流程：OPEN → IN_PROGRESS → VERIFIED → CLOSED，支持根因分析、纠正措施记录、
 * 验证闭环和超时自动升级。 /
 * Correction flow: OPEN → IN_PROGRESS → VERIFIED → CLOSED, with root cause analysis,
 * corrective action recording, verification closure and auto-escalation on timeout.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierImprovementService {

    private final SupplierImprovementMapper improvementMapper;

    /** 合法状态 / Valid statuses */
    private static final List<String> VALID_STATUSES = List.of("OPEN", "IN_PROGRESS", "VERIFIED", "CLOSED");

    // ==================== 创建整改单 / Create Improvement ====================

    /**
     * 创建整改单 / Create improvement order
     *
     * @param improvement 整改对象 / Improvement object
     * @return 创建后的整改 / Created improvement
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierImprovement create(SupplierImprovement improvement) {
        improvement.setStatus("OPEN");
        improvement.setCreatedAt(LocalDateTime.now());
        improvement.setUpdatedAt(LocalDateTime.now());
        improvementMapper.insert(improvement);
        log.info("创建整改单: supplierId={}, title={}, deadline={}",
            improvement.getSupplierId(), improvement.getTitle(), improvement.getDeadline());
        return improvement;
    }

    // ==================== 记录措施 / Record Action ====================

    /**
     * 记录根因分析 / Record root cause analysis
     *
     * @param id        整改单ID / Improvement ID
     * @param rootCause 根因分析 / Root cause analysis
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordRootCause(Long id, String rootCause) {
        checkStatus(id, "OPEN");
        improvementMapper.update(null,
            new LambdaUpdateWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getId, id)
                .set(SupplierImprovement::getRootCause, rootCause)
                .set(SupplierImprovement::getUpdatedAt, LocalDateTime.now()));
        log.info("记录根因分析: id={}", id);
    }

    /**
     * 记录纠正措施并推进到进行中 / Record corrective action and advance to IN_PROGRESS
     *
     * @param id               整改单ID / Improvement ID
     * @param correctiveAction 纠正措施 / Corrective action
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordCorrectiveAction(Long id, String correctiveAction) {
        checkStatus(id, "OPEN");
        improvementMapper.update(null,
            new LambdaUpdateWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getId, id)
                .set(SupplierImprovement::getCorrectiveAction, correctiveAction)
                .set(SupplierImprovement::getStatus, "IN_PROGRESS")
                .set(SupplierImprovement::getUpdatedAt, LocalDateTime.now()));
        log.info("记录纠正措施并推进: id={}", id);
    }

    // ==================== 验证闭环 / Verify Closure ====================

    /**
     * 验证闭环 / Verify and close
     *
     * @param id           整改单ID / Improvement ID
     * @param verifierId   验证人ID / Verifier ID
     * @param verifierName 验证人姓名 / Verifier name
     */
    @Transactional(rollbackFor = Exception.class)
    public void verify(Long id, Long verifierId, String verifierName) {
        checkStatus(id, "IN_PROGRESS");
        improvementMapper.update(null,
            new LambdaUpdateWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getId, id)
                .set(SupplierImprovement::getStatus, "VERIFIED")
                .set(SupplierImprovement::getVerifierId, verifierId)
                .set(SupplierImprovement::getVerifierName, verifierName)
                .set(SupplierImprovement::getVerifiedAt, LocalDateTime.now())
                .set(SupplierImprovement::getUpdatedAt, LocalDateTime.now()));
        log.info("验证完成: id={}, verifier={}", id, verifierName);
    }

    /**
     * 关闭整改单 / Close improvement order
     *
     * @param id 整改单ID / Improvement ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        checkStatus(id, "VERIFIED");
        improvementMapper.update(null,
            new LambdaUpdateWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getId, id)
                .set(SupplierImprovement::getStatus, "CLOSED")
                .set(SupplierImprovement::getClosedAt, LocalDateTime.now())
                .set(SupplierImprovement::getUpdatedAt, LocalDateTime.now()));
        log.info("整改单已关闭: id={}", id);
    }

    // ==================== 查询 / Query ====================

    /**
     * 按供应商查询所有整改单 / Query all improvements for a supplier
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 整改单列表（按创建时间降序） / Improvement list (by created time desc)
     */
    public List<SupplierImprovement> listBySupplierId(Long supplierId) {
        return improvementMapper.selectList(
            new LambdaQueryWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getSupplierId, supplierId)
                .orderByDesc(SupplierImprovement::getCreatedAt));
    }

    /**
     * 按状态查询整改单 / Query improvements by status
     *
     * @param status 状态: OPEN/IN_PROGRESS/VERIFIED/CLOSED / Status
     * @return 整改单列表 / Improvement list
     */
    public List<SupplierImprovement> listByStatus(String status) {
        return improvementMapper.selectList(
            new LambdaQueryWrapper<SupplierImprovement>()
                .eq(SupplierImprovement::getStatus, status)
                .orderByDesc(SupplierImprovement::getCreatedAt));
    }

    /**
     * 查询超期未关闭的整改单 / Query overdue unclosed improvements
     *
     * @return 超期整改单列表 / Overdue improvement list
     */
    public List<SupplierImprovement> listOverdue() {
        return improvementMapper.selectList(
            new LambdaQueryWrapper<SupplierImprovement>()
                .lt(SupplierImprovement::getDeadline, LocalDate.now())
                .notIn(SupplierImprovement::getStatus, List.of("CLOSED"))
                .orderByAsc(SupplierImprovement::getDeadline));
    }

    /**
     * 按主键查询 / Query by primary key
     */
    public SupplierImprovement getById(Long id) {
        SupplierImprovement improvement = improvementMapper.selectById(id);
        if (improvement == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "整改单不存在: " + id);
        }
        return improvement;
    }

    // ==================== 内部方法 / Internal ====================

    /**
     * 校验当前状态 / Validate current status
     */
    private void checkStatus(Long id, String expectedStatus) {
        SupplierImprovement improvement = improvementMapper.selectById(id);
        if (improvement == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "整改单不存在: " + id);
        }
        if (!expectedStatus.equals(improvement.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE,
                "当前状态为 " + improvement.getStatus() + "，期望状态为 " + expectedStatus);
        }
    }
}
