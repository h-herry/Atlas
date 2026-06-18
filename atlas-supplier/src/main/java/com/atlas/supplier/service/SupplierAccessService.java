package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.*;
import com.atlas.supplier.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商准入管理 Service — 发布招募公告 → 供应商注册 → 初审 → 现场考察 → 终审 → 自动入库 /
 * Supplier access management Service — publish recruit notice → supplier registration → initial review → site inspection → final review → auto-onboard
 *
 * <p>审批流转状态机: / Approval status state machine:
 * <pre>
 *   待审核(0) → 初审通过(1) → 现场考察(2) → 终审通过(3) → 已入库(5)
 *   Pending(0) → Initial passed(1) → Site inspected(2) → Final approved(3) → Onboarded(5)
 *       ↓              ↓              ↓
 *    驳回(4)       驳回(4)        驳回(4)
 *    Rejected(4)   Rejected(4)    Rejected(4)
 * </pre>
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierAccessService {

    private final RecruitNoticeMapper recruitNoticeMapper;
    private final SupplierRegisterMapper supplierRegisterMapper;
    private final SupplierApprovalRecordMapper approvalRecordMapper;
    private final SupplierMapper supplierMapper;
    private final SupplierBlacklistMapper blacklistMapper;

    // ==================== 招募公告 / Recruit Notice ====================

    /**
     * 发布招募公告 / Publish recruit notice
     */
    @Transactional
    public RecruitNotice publishNotice(RecruitNotice notice) {
        notice.setStatus(1); // 已发布 / Published
        notice.setPublishTime(LocalDateTime.now());
        recruitNoticeMapper.insert(notice);
        log.info("招募公告已发布: noticeNo={}, title={}", notice.getNoticeNo(), notice.getTitle());
        return notice;
    }

    /**
     * 分页查询招募公告 / Paginated query of recruit notices
     */
    public Page<RecruitNotice> pageNotice(Integer status, int page, int size) {
        LambdaQueryWrapper<RecruitNotice> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RecruitNotice::getStatus, status);
        }
        wrapper.orderByDesc(RecruitNotice::getCreatedAt);
        return recruitNoticeMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 关闭招募公告 / Close recruit notice
     */
    @Transactional
    public void closeNotice(Long noticeId) {
        RecruitNotice notice = recruitNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BizException(ErrorCode.RECRUIT_NOTICE_NOT_EXIST);
        }
        notice.setStatus(3);
        recruitNoticeMapper.updateById(notice);
    }

    // ==================== 供应商注册/准入 / Supplier Registration ====================

    /**
     * 供应商提交注册申请 / Supplier submits registration
     */
    @Transactional
    public SupplierRegister submitRegister(SupplierRegister register) {
        // 黑名单校验 / Blacklist check
        if (register.getCreditCode() != null) {
            checkBlacklistByRegister(register);
        }
        register.setApprovalStatus(0); // 待审核 / Pending
        supplierRegisterMapper.insert(register);
        log.info("供应商注册申请已提交: supplierName={}", register.getSupplierName());
        return register;
    }

    /**
     * 分页查询注册申请 / Paginated query of registrations
     */
    public Page<SupplierRegister> pageRegister(Integer approvalStatus, int page, int size) {
        LambdaQueryWrapper<SupplierRegister> wrapper = new LambdaQueryWrapper<>();
        if (approvalStatus != null) {
            wrapper.eq(SupplierRegister::getApprovalStatus, approvalStatus);
        }
        wrapper.orderByDesc(SupplierRegister::getCreatedAt);
        return supplierRegisterMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 审批流转 / Approval Workflow ====================

    /**
     * 初审 — 资质审查 / Initial review — qualification check
     */
    @Transactional
    public void initialReview(Long registerId, Integer result, BigDecimal score,
                              String comment, Long approverId, String approverName, String approverDept) {
        SupplierRegister register = getRegister(registerId);
        if (register.getApprovalStatus() != 0) {
            throw new BizException(ErrorCode.REGISTER_ALREADY_APPROVED);
        }

        if (result == 1) { // 通过 / Passed
            register.setApprovalStatus(1); // 初审通过 / Initial passed
        } else {
            register.setApprovalStatus(4); // 驳回 / Rejected
            register.setRejectReason(comment);
        }
        register.setReviewerId(approverId);
        register.setReviewerName(approverName);
        register.setReviewedAt(LocalDateTime.now());
        supplierRegisterMapper.updateById(register);

        saveApprovalRecord(registerId, "INITIAL_REVIEW", result, score, comment, approverId, approverName, approverDept);
        log.info("初审完成: registerId={}, result={}", registerId, result);
    }

    /**
     * 现场考察 / Site inspection
     */
    @Transactional
    public void fieldInspect(Long registerId, Integer result, BigDecimal score,
                             String comment, Long approverId, String approverName, String approverDept) {
        SupplierRegister register = getRegister(registerId);
        if (register.getApprovalStatus() != 1) {
            throw new BizException(ErrorCode.APPROVAL_NODE_INVALID);
        }

        if (result == 1) {
            register.setApprovalStatus(2); // 现场考察通过 / Site inspection passed
        } else {
            register.setApprovalStatus(4);
            register.setRejectReason(comment);
        }
        register.setReviewerId(approverId);
        register.setReviewerName(approverName);
        register.setReviewedAt(LocalDateTime.now());
        supplierRegisterMapper.updateById(register);

        saveApprovalRecord(registerId, "FIELD_INSPECT", result, score, comment, approverId, approverName, approverDept);
        log.info("现场考察完成: registerId={}, result={}", registerId, result);
    }

    /**
     * 终审 — 通过后自动入库 / Final review — auto-onboard on approval
     */
    @Transactional
    public void finalReview(Long registerId, Integer result, BigDecimal score,
                            String comment, Long approverId, String approverName, String approverDept) {
        SupplierRegister register = getRegister(registerId);
        if (register.getApprovalStatus() != 2) {
            throw new BizException(ErrorCode.APPROVAL_NODE_INVALID);
        }

        if (result == 1) {
            register.setApprovalStatus(3); // 终审通过 / Final approved
            // 自动入库：创建 Supplier 记录 / Auto-onboard: create Supplier record
            createSupplierFromRegister(register);
            register.setApprovalStatus(5); // 已入库 / Onboarded
        } else {
            register.setApprovalStatus(4);
            register.setRejectReason(comment);
        }
        register.setReviewerId(approverId);
        register.setReviewerName(approverName);
        register.setReviewedAt(LocalDateTime.now());
        supplierRegisterMapper.updateById(register);

        saveApprovalRecord(registerId, "FINAL_REVIEW", result, score, comment, approverId, approverName, approverDept);
        log.info("终审完成: registerId={}, result={}, autoOnboard={}", registerId, result, result == 1);
    }

    // ==================== 内部方法 / Internal Methods ====================

    private SupplierRegister getRegister(Long registerId) {
        SupplierRegister register = supplierRegisterMapper.selectById(registerId);
        if (register == null) {
            throw new BizException(ErrorCode.REGISTER_NOT_EXIST);
        }
        return register;
    }

    private void saveApprovalRecord(Long registerId, String node, Integer result, BigDecimal score,
                                     String comment, Long approverId, String approverName, String approverDept) {
        SupplierApprovalRecord record = new SupplierApprovalRecord();
        record.setRegisterId(registerId);
        record.setApprovalNode(node);
        record.setApprovalResult(result);
        record.setScore(score);
        record.setComment(comment);
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setApproverDept(approverDept);
        record.setApprovedAt(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    /**
     * 从注册信息创建供应商主表记录（自动入库） / Create supplier master record from registration (auto-onboard)
     */
    private void createSupplierFromRegister(SupplierRegister register) {
        Supplier supplier = new Supplier();
        supplier.setSupplierName(register.getSupplierName());
        supplier.setContactPerson(register.getContactPerson());
        supplier.setContactPhone(register.getContactPhone());
        supplier.setEmail(register.getEmail());
        supplier.setAddress(register.getAddress());
        supplier.setStatus(1); // 已准入 / Admitted
        supplierMapper.insert(supplier);
        log.info("供应商自动入库: supplierId={}, supplierName={}", supplier.getId(), supplier.getSupplierName());
    }

    /**
     * 注册前黑名单检查（按信用代码/企业名精确匹配） / Blacklist check before registration (exact match by credit code / company name)
     */
    private void checkBlacklistByRegister(SupplierRegister register) {
        // 按信用代码或企业名称精确匹配生效的黑名单记录 / Match active blacklist by credit code or company name
        boolean inBlacklist = blacklistMapper.existsActiveByCreditCodeOrName(
                register.getCreditCode(), register.getSupplierName());
        if (inBlacklist) {
            throw new BizException(ErrorCode.SUPPLIER_BLACKLISTED);
        }
    }
}
