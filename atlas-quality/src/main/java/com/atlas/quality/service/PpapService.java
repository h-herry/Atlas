package com.atlas.quality.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.quality.entity.PpapElement;
import com.atlas.quality.entity.PpapSubmission;
import com.atlas.quality.mapper.PpapElementMapper;
import com.atlas.quality.mapper.PpapSubmissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PPAP提交跟踪服务 — 18要素清单管理、逐项审核、级别1~5支持 /
 * PPAP submission tracking service — 18-element checklist mgmt, item review, level 1~5 support
 * <p>
 * PPAP (生产件批准程序) 是 IATF 16949 强制要求的零部件批准流程。 /
 * PPAP (Production Part Approval Process) is IATF 16949 mandatory part approval process.
 * <p>
 * PPAP 等级要求（要素数量随等级升高减少）： /
 * PPAP level requirements (element count decreases as level increases):
 * <ul>
 *   <li>Level 1: 仅提交保证书(PSW) — 1 要素 / Only PSW — 1 element</li>
 *   <li>Level 2: PSW + 有限文件(7要素) / PSW + limited docs (7 elements)</li>
 *   <li>Level 3: 标准提交(14要素) / Standard submission (14 elements)</li>
 *   <li>Level 4: PSW + 客户指定文件(8要素) / PSW + customer-specified (8 elements)</li>
 *   <li>Level 5: 完整提交(18要素) / Full submission (18 elements)</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PpapService {

    private final PpapSubmissionMapper submissionMapper;
    private final PpapElementMapper elementMapper;

    /**
     * PPAP 18 要素预置清单 / PPAP 18-element preset checklist
     * <p>
     * 基于 AIAG PPAP 第四版定义的标准18项要素。 /
     * Based on AIAG PPAP 4th Edition standard 18 elements.
     */
    public static final List<ElementDef> PPAP_18_ELEMENTS = List.of(
            new ElementDef(1,  "DESIGN_RECORD",        "设计记录 / Design Record"),
            new ElementDef(2,  "ENG_CHANGE_DOC",       "工程变更授权文件 / Engineering Change Documents"),
            new ElementDef(3,  "CUSTOMER_APPROVAL",    "客户工程批准 / Customer Engineering Approval"),
            new ElementDef(4,  "DFMEA",                "设计FMEA / Design FMEA"),
            new ElementDef(5,  "PROCESS_FLOW",         "过程流程图 / Process Flow Diagram"),
            new ElementDef(6,  "PFMEA",                "过程FMEA / Process FMEA"),
            new ElementDef(7,  "CONTROL_PLAN",         "控制计划 / Control Plan"),
            new ElementDef(8,  "MSA",                  "测量系统分析 / Measurement System Analysis (MSA)"),
            new ElementDef(9,  "DIMENSIONAL_RESULTS",  "尺寸结果 / Dimensional Results"),
            new ElementDef(10, "MATERIAL_TEST",        "材料/性能试验 / Material Performance Test"),
            new ElementDef(11, "INITIAL_STUDY",        "初始过程研究 / Initial Process Study"),
            new ElementDef(12, "QUALIFIED_LAB",        "合格实验室文件 / Qualified Laboratory Documentation"),
            new ElementDef(13, "APPEARANCE_APPROVAL",  "外观批准报告 / Appearance Approval Report (AAR)"),
            new ElementDef(14, "SAMPLE_PRODUCT",       "样品产品 / Sample Product"),
            new ElementDef(15, "MASTER_SAMPLE",        "标准样品 / Master Sample"),
            new ElementDef(16, "CHECKING_AID",         "检具 / Checking Aids"),
            new ElementDef(17, "CUSTOMER_REQUIREMENT", "顾客特殊要求 / Customer-Specific Requirements"),
            new ElementDef(18, "PSW",                  "零件提交保证书 / Part Submission Warrant (PSW)")
    );

    /**
     * 创建 PPAP 提交记录（含自动生成要素清单） / Create PPAP submission with auto-generated element checklist
     *
     * @param submission PPAP提交实体 / PPAP submission entity
     * @return 创建后的提交记录 / Created submission
     */
    @Transactional(rollbackFor = Exception.class)
    public PpapSubmission createSubmission(PpapSubmission submission) {
        submission.setStatus(PpapSubmission.STATUS_PENDING);
        submissionMapper.insert(submission);

        // 根据 PPAP 等级生成要素清单 / Generate element checklist based on PPAP level
        Set<Integer> requiredSeqs = getRequiredElementSeqs(submission.getPpapLevel());
        for (ElementDef def : PPAP_18_ELEMENTS) {
            PpapElement element = new PpapElement();
            element.setSubmissionId(submission.getId());
            element.setElementCode(def.code);
            element.setElementName(def.name);
            element.setElementSeq(def.seq);
            element.setIsRequired(requiredSeqs.contains(def.seq) ? 1 : 0);
            element.setSubmitted(0);
            element.setApproved(0);
            elementMapper.insert(element);
        }

        log.info("PPAP提交已创建: id={} supplier={} material={} level={}",
                submission.getId(), submission.getSupplierId(), submission.getMaterialId(), submission.getPpapLevel());
        return submission;
    }

    /**
     * 供应商提交 PPAP 要素文件 / Supplier submits PPAP element file
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @param elementCode  要素编码 / Element code
     * @param filePath     文件路径 / File path
     * @return 更新后的要素 / Updated element
     */
    @Transactional(rollbackFor = Exception.class)
    public PpapElement submitElement(Long submissionId, String elementCode, String filePath) {
        PpapSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "PPAP提交记录不存在");
        }

        PpapElement element = elementMapper.selectOne(
                new LambdaQueryWrapper<PpapElement>()
                        .eq(PpapElement::getSubmissionId, submissionId)
                        .eq(PpapElement::getElementCode, elementCode));
        if (element == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "PPAP要素不存在: " + elementCode);
        }

        element.setSubmitted(1);
        element.setFilePath(filePath);
        elementMapper.updateById(element);

        // 首次提交时更新主状态 / Update master status on first submission
        if (PpapSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            submission.setStatus(PpapSubmission.STATUS_SUBMITTED);
            submission.setSubmissionDate(LocalDate.now());
            submissionMapper.updateById(submission);
        }

        log.info("PPAP要素已提交: submissionId={} element={}", submissionId, elementCode);
        return element;
    }

    /**
     * 质量工程师审核 PPAP 要素（通过/退回） / Quality engineer reviews PPAP element (approve/return)
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @param elementCode  要素编码 / Element code
     * @param approved     是否通过: true-通过 false-退回 / Whether approved: true-approve false-return
     * @param comment      审核意见 / Review comment
     * @param reviewerId   审核人ID / Reviewer ID
     * @return 更新后的要素 / Updated element
     */
    @Transactional(rollbackFor = Exception.class)
    public PpapElement reviewElement(Long submissionId, String elementCode, boolean approved,
                                      String comment, Long reviewerId) {
        PpapElement element = elementMapper.selectOne(
                new LambdaQueryWrapper<PpapElement>()
                        .eq(PpapElement::getSubmissionId, submissionId)
                        .eq(PpapElement::getElementCode, elementCode));
        if (element == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "PPAP要素不存在: " + elementCode);
        }

        element.setApproved(approved ? 1 : 0);
        element.setComment(comment);
        element.setReviewedBy(reviewerId);
        element.setReviewedAt(LocalDateTime.now());
        elementMapper.updateById(element);

        // 更新主状态 / Update master status
        updateSubmissionStatus(submissionId);

        log.info("PPAP要素审核完成: submissionId={} element={} approved={}", submissionId, elementCode, approved);
        return element;
    }

    /**
     * 更新 PPAP 提交主状态 / Update PPAP submission master status
     */
    private void updateSubmissionStatus(Long submissionId) {
        PpapSubmission submission = submissionMapper.selectById(submissionId);
        List<PpapElement> elements = elementMapper.selectList(
                new LambdaQueryWrapper<PpapElement>()
                        .eq(PpapElement::getSubmissionId, submissionId));

        // 仅统计必需要素 / Only count required elements
        List<PpapElement> requiredElements = elements.stream()
                .filter(e -> e.getIsRequired() == 1)
                .toList();

        long approvedCount = requiredElements.stream().filter(e -> e.getApproved() == 1).count();
        long rejectedCount = requiredElements.stream()
                .filter(e -> e.getApproved() == 0 && e.getSubmitted() == 1 && e.getReviewedBy() != null).count();

        if (rejectedCount > 0) {
            submission.setStatus(PpapSubmission.STATUS_REJECTED);
        } else if (approvedCount == requiredElements.size()) {
            submission.setStatus(PpapSubmission.STATUS_FULLY_APPROVED);
            submission.setApprovalDate(LocalDate.now());
        } else if (approvedCount > 0) {
            submission.setStatus(PpapSubmission.STATUS_UNDER_REVIEW);
        }

        submissionMapper.updateById(submission);
    }

    /**
     * 查询 PPAP 提交及要素清单 / Query PPAP submission with element checklist
     *
     * @param submissionId PPAP提交ID / PPAP submission ID
     * @return 提交详情(含要素列表) / Submission detail (with element list)
     */
    public Map<String, Object> getSubmissionDetail(Long submissionId) {
        PpapSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "PPAP提交记录不存在");
        }

        List<PpapElement> elements = elementMapper.selectList(
                new LambdaQueryWrapper<PpapElement>()
                        .eq(PpapElement::getSubmissionId, submissionId)
                        .orderByAsc(PpapElement::getElementSeq));

        // 统计 / Statistics
        long requiredCount = elements.stream().filter(e -> e.getIsRequired() == 1).count();
        long submittedCount = elements.stream().filter(e -> e.getSubmitted() == 1 && e.getIsRequired() == 1).count();
        long approvedCount = elements.stream().filter(e -> e.getApproved() == 1 && e.getIsRequired() == 1).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submission", submission);
        result.put("elements", elements);
        result.put("progress", Map.of(
                "requiredCount", requiredCount,
                "submittedCount", submittedCount,
                "approvedCount", approvedCount,
                "progressPercent", requiredCount > 0 ? (int)(approvedCount * 100 / requiredCount) : 0
        ));
        return result;
    }

    /**
     * 按供应商分页查询 PPAP 提交列表 / Query PPAP submissions by supplier (paginated)
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param status     状态过滤 / Status filter
     * @param page       当前页 / Current page
     * @param size       每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<PpapSubmission> listBySupplier(Long supplierId, String status, int page, int size) {
        LambdaQueryWrapper<PpapSubmission> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(PpapSubmission::getSupplierId, supplierId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(PpapSubmission::getStatus, status);
        }
        wrapper.orderByDesc(PpapSubmission::getCreatedAt);
        return submissionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取指定 PPAP 等级要求的要素序号集合 / Get required element sequence set for given PPAP level
     */
    private static Set<Integer> getRequiredElementSeqs(int level) {
        return switch (level) {
            case 1 -> Set.of(18);                          // 仅 PSW / Only PSW
            case 2 -> Set.of(1, 6, 7, 8, 9, 10, 18);      // 7 elements
            case 3 -> Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 18); // 14 elements
            case 4 -> Set.of(1, 2, 3, 4, 5, 6, 7, 18);    // 8 elements
            case 5 -> Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18); // All 18
            default -> throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "PPAP等级必须为1~5");
        };
    }

    /**
     * 要素定义 / Element definition
     */
    public record ElementDef(int seq, String code, String name) {}
}
