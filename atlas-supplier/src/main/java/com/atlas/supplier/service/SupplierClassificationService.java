package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierClassification;
import com.atlas.supplier.mapper.SupplierClassificationMapper;
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
 * 供应商分级 Service — 定级 / 升降级 / 到期重评提醒 /
 * Supplier classification Service — grading / promotion-demotion / expiry review reminder
 *
 * <p>四级分级：战略(STRATEGIC) / 核心(CORE) / 一般(GENERAL) / 潜在(POTENTIAL)，
 * 联动绩效评分卡自动升降级，有效期 1 年，到期前 30 天提醒重评。 /
 * Four tiers: STRATEGIC / CORE / GENERAL / POTENTIAL,
 * linked with scorecard for auto promotion/demotion, 1-year validity, 30-day pre-expiry reminder.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierClassificationService {

    private final SupplierClassificationMapper classificationMapper;

    /** 分级有效期（天） / Classification validity (days) */
    private static final int VALIDITY_DAYS = 365;

    /** 到期提前提醒天数 / Pre-expiry reminder days */
    private static final int REMINDER_DAYS = 30;

    /** 有效分级 / Valid grade tiers */
    private static final List<String> VALID_GRADES = List.of("STRATEGIC", "CORE", "GENERAL", "POTENTIAL");

    // ==================== 定级 / Grading ====================

    /**
     * 供应商定级 / Classify supplier
     *
     * <p>创建新的分级记录，自动将旧分级记录置为失效。 /
     * Creates new classification record, auto-invalidates previous one.</p>
     *
     * @param classification 分级对象 / Classification object
     * @return 创建后的分级 / Created classification
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierClassification classify(SupplierClassification classification) {
        // 校验分级值 / Validate grade value
        if (!VALID_GRADES.contains(classification.getGrade())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                "无效分级: " + classification.getGrade() + "，有效值: " + String.join(", ", VALID_GRADES));
        }

        // 查询当前有效分级作为 prevGrade / Query current active classification as prevGrade
        SupplierClassification current = getCurrentActive(classification.getSupplierId());
        if (current != null) {
            classification.setPrevGrade(current.getGrade());
            // 旧记录失效 / Invalidate old record
            invalidate(current.getId());
        }

        // 设置有效期 / Set validity
        if (classification.getAssessedDate() == null) {
            classification.setAssessedDate(LocalDate.now());
        }
        if (classification.getValidUntil() == null) {
            classification.setValidUntil(classification.getAssessedDate().plusDays(VALIDITY_DAYS));
        }
        classification.setStatus(1);
        classification.setCreatedAt(LocalDateTime.now());
        classification.setUpdatedAt(LocalDateTime.now());
        classificationMapper.insert(classification);

        log.info("供应商定级: supplierId={}, grade={}, prevGrade={}",
            classification.getSupplierId(), classification.getGrade(), classification.getPrevGrade());
        return classification;
    }

    /**
     * 升降级 / Promotion or demotion
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param newGrade   新等级 / New grade
     * @param reason     升降级原因 / Reason
     * @param assessorId 审批人ID / Assessor ID
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierClassification promoteOrDemote(Long supplierId, String newGrade,
                                                    String reason, Long assessorId, String assessorName) {
        SupplierClassification classification = new SupplierClassification();
        classification.setSupplierId(supplierId);
        classification.setGrade(newGrade);
        classification.setReason(reason);
        classification.setAssessorId(assessorId);
        classification.setAssessorName(assessorName);
        classification.setAssessedDate(LocalDate.now());
        return classify(classification);
    }

    // ==================== 查询 / Query ====================

    /**
     * 查询供应商当前有效分级 / Query current active classification for supplier
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 当前分级（可能为 null） / Current classification (nullable)
     */
    public SupplierClassification getCurrentActive(Long supplierId) {
        return classificationMapper.selectOne(
            new LambdaQueryWrapper<SupplierClassification>()
                .eq(SupplierClassification::getSupplierId, supplierId)
                .eq(SupplierClassification::getStatus, 1)
                .orderByDesc(SupplierClassification::getCreatedAt)
                .last("LIMIT 1"));
    }

    /**
     * 查询供应商分级历史 / Query classification history for supplier
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 分级历史列表（按评定日期降序） / History list (by assessed date desc)
     */
    public List<SupplierClassification> listHistory(Long supplierId) {
        return classificationMapper.selectList(
            new LambdaQueryWrapper<SupplierClassification>()
                .eq(SupplierClassification::getSupplierId, supplierId)
                .orderByDesc(SupplierClassification::getAssessedDate));
    }

    // ==================== 到期重评提醒 / Expiry Review Reminder ====================

    /**
     * 查询即将到期的分级（用于提醒重评） / Query classifications nearing expiry (for review reminder)
     *
     * <p>有效期内且距到期日在 REMINDER_DAYS 天内的所有分级记录。 /
     * All active classifications with expiry within REMINDER_DAYS days.</p>
     *
     * @return 即将到期的分级列表 / List of classifications nearing expiry
     */
    public List<SupplierClassification> listExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate remindStart = today;
        LocalDate remindEnd = today.plusDays(REMINDER_DAYS);
        return classificationMapper.selectList(
            new LambdaQueryWrapper<SupplierClassification>()
                .eq(SupplierClassification::getStatus, 1)
                .ge(SupplierClassification::getValidUntil, remindStart)
                .le(SupplierClassification::getValidUntil, remindEnd)
                .orderByAsc(SupplierClassification::getValidUntil));
    }

    // ==================== 内部方法 / Internal ====================

    /**
     * 使旧分级记录失效 / Invalidate old classification record
     */
    private void invalidate(Long id) {
        classificationMapper.update(null,
            new LambdaUpdateWrapper<SupplierClassification>()
                .eq(SupplierClassification::getId, id)
                .set(SupplierClassification::getStatus, 0)
                .set(SupplierClassification::getUpdatedAt, LocalDateTime.now()));
    }
}
