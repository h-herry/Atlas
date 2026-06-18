package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierRectification;
import com.atlas.supplier.mapper.SupplierRectificationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 供应商整改跟踪 Service / Supplier rectification tracking Service
 *
 * <p>全生命周期整改管理：创建整改单 → 供应商提交方案 → 复评 → 关联绩效考核。
 * 支持 8D 报告模式的问题闭环追溯。 /
 * Full-lifecycle rectification management: create rectification order → supplier submits plan → review → linked to performance evaluation.
 * Supports 8D report-based closed-loop issue tracing.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierRectificationService extends ServiceImpl<SupplierRectificationMapper, SupplierRectification> {

    private final SupplierRectificationMapper rectificationMapper;

    /**
     * 分页查询整改单 / Paginated query of rectification orders
     */
    public Page<SupplierRectification> page(Long supplierId, Integer status, int page, int size) {
        LambdaQueryWrapper<SupplierRectification> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierRectification::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(SupplierRectification::getStatus, status);
        }
        wrapper.orderByDesc(SupplierRectification::getCreatedAt);
        return rectificationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 按供应商查询整改单 / List rectification orders by supplier
     */
    public java.util.List<SupplierRectification> listBySupplierId(Long supplierId) {
        LambdaQueryWrapper<SupplierRectification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierRectification::getSupplierId, supplierId)
               .orderByDesc(SupplierRectification::getCreatedAt);
        return rectificationMapper.selectList(wrapper);
    }

    /**
     * 创建整改单 — 初始状态 0(待整改) /
     * Create rectification order — initial status 0 (pending)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createRectification(SupplierRectification rectification) {
        rectification.setStatus(0);
        return save(rectification);
    }

    /**
     * 供应商提交整改方案 — 状态 0→1 /
     * Supplier submits rectification plan — status 0→1
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPlan(Long id, String plan, String evidenceUrl) {
        SupplierRectification rectification = getById(id);
        if (rectification == null) {
            throw new BizException(ErrorCode.RECTIFICATION_NOT_EXIST);
        }
        if (rectification.getStatus() != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "仅待整改状态可提交方案，当前状态: " + rectification.getStatus());
        }
        rectification.setRectificationPlan(plan);
        rectification.setEvidenceUrl(evidenceUrl);
        rectification.setStatus(1);
        return updateById(rectification);
    }

    /**
     * 审核人确认方案，进入整改中 — 状态 1→2 /
     * Reviewer confirms plan, enters rectification in progress — status 1→2
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean startRectification(Long id, Long auditorId) {
        SupplierRectification rectification = getById(id);
        if (rectification == null) {
            throw new BizException(ErrorCode.RECTIFICATION_NOT_EXIST);
        }
        if (rectification.getStatus() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "仅方案已提交状态可开始整改，当前状态: " + rectification.getStatus());
        }
        rectification.setStatus(2);
        rectification.setAuditorId(auditorId);
        return updateById(rectification);
    }

    /**
     * 整改完成，提交复评 — 状态 2→3 /
     * Rectification complete, submit for review — status 2→3
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForReview(Long id, String evidenceUrl) {
        SupplierRectification rectification = getById(id);
        if (rectification == null) {
            throw new BizException(ErrorCode.RECTIFICATION_NOT_EXIST);
        }
        if (rectification.getStatus() != 2) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "仅整改中状态可提交复评，当前状态: " + rectification.getStatus());
        }
        rectification.setStatus(3);
        rectification.setEvidenceUrl(evidenceUrl);
        return updateById(rectification);
    }

    /**
     * 复评 — 状态 3→4(PASS) / 状态3→2(FAIL，退回重新整改) / 3→6(EXTEND延期) /
     * Review — status 3→4(PASS) / 3→2(FAIL, return for rework) / 3→3(EXTEND, keep pending)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long id, String result, String comment) {
        SupplierRectification rectification = getById(id);
        if (rectification == null) {
            throw new BizException(ErrorCode.RECTIFICATION_NOT_EXIST);
        }
        if (rectification.getStatus() != 3) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "仅待复评状态可进行复评，当前状态: " + rectification.getStatus());
        }
        switch (result.toUpperCase()) {
            case "PASS":
                rectification.setStatus(4);
                break;
            case "FAIL":
                rectification.setStatus(2);
                break;
            case "EXTEND":
                rectification.setStatus(3);
                break;
            default:
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "无效的复评结果: " + result);
        }
        rectification.setResult(result);
        return updateById(rectification);
    }

    /**
     * 创建整改单（别名） / Create rectification order (alias)
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierRectification create(SupplierRectification rectification) {
        createRectification(rectification);
        return rectification;
    }

    /**
     * 复评 / Re-evaluate
     */
    @Transactional(rollbackFor = Exception.class)
    public void reEvaluate(Long id, Integer result, String evaluator) {
        log.info("复评: id={}, result={}, evaluator={}", id, result, evaluator);
        String resultStr;
        switch (result) {
            case 1: resultStr = "PASS"; break;
            case 2: resultStr = "FAIL"; break;
            case 3: resultStr = "EXTEND"; break;
            default:
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "无效的复评结果: " + result);
        }
        review(id, resultStr, "复评人: " + evaluator);
    }
}
