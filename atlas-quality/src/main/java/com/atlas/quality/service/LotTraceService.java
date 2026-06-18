package com.atlas.quality.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.material.entity.LotTrace;
import com.atlas.material.mapper.LotTraceMapper;
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
 * 来料批次追溯服务 — 正向/反向追溯、批次锁定 /
 * Incoming lot trace service — forward/reverse trace, lot locking
 * <p>
 * 汽车零部件制造要求严格批次管理，实现： /
 * Automotive parts manufacturing strict lot management, implements:
 * <ul>
 *   <li><b>正向追溯</b>: lot_no → 供应商 / 订单 / 收货日期 / 检验结果 / Forward: lot_no → supplier / order / receive date / inspection</li>
 *   <li><b>反向追溯</b>: material_id → 所有关联批次 → 供应商/订单列表 / Reverse: material_id → all lots → supplier/order list</li>
 *   <li><b>质检不合格锁定</b>: 按批次号快速锁定受影响范围 / Quick lock: on inspection failure, lock affected scope by lot</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotTraceService {

    private final LotTraceMapper lotTraceMapper;

    /**
     * 正向追溯：按批次号查询全链路信息 / Forward trace: query full chain by lot number
     *
     * @param lotNo 批次号 / Lot number
     * @return 全链路追溯信息 / Full chain trace info
     */
    public Map<String, Object> forwardTrace(String lotNo) {
        LotTrace lot = lotTraceMapper.selectOne(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getLotNo, lotNo));

        if (lot == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "批次不存在: " + lotNo);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lotNo", lot.getLotNo());
        result.put("materialId", lot.getMaterialId());
        result.put("supplierId", lot.getSupplierId());
        result.put("orderId", lot.getOrderId());
        result.put("receiveDate", lot.getReceiveDate());
        result.put("quantity", lot.getQuantity());
        result.put("status", lot.getStatus());
        result.put("inspectionResult", lot.getInspectionResult());
        result.put("inspectionDate", lot.getInspectionDate());
        result.put("rejectionReason", lot.getRejectionReason());
        return result;
    }

    /**
     * 反向追溯：按物料ID查询所有关联批次 / Reverse trace: query all related lots by material ID
     *
     * @param materialId 物料ID / Material ID
     * @return 批次列表 / Lot list
     */
    public List<LotTrace> reverseByMaterial(Long materialId) {
        return lotTraceMapper.selectList(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getMaterialId, materialId)
                        .orderByDesc(LotTrace::getReceiveDate));
    }

    /**
     * 反向追溯：按供应商ID查询所有关联批次 / Reverse trace: query all related lots by supplier ID
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 批次列表 / Lot list
     */
    public List<LotTrace> reverseBySupplier(Long supplierId) {
        return lotTraceMapper.selectList(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getSupplierId, supplierId)
                        .orderByDesc(LotTrace::getReceiveDate));
    }

    /**
     * 反向追溯：按订单ID查询所有关联批次 / Reverse trace: query all related lots by order ID
     *
     * @param orderId 订单ID / Order ID
     * @return 批次列表 / Lot list
     */
    public List<LotTrace> reverseByOrder(Long orderId) {
        return lotTraceMapper.selectList(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getOrderId, orderId)
                        .orderByDesc(LotTrace::getReceiveDate));
    }

    /**
     * 创建批次记录（收货时调用） / Create lot record (called on receipt)
     *
     * @param lot 批次实体 / Lot trace entity
     * @return 创建后的批次 / Created lot
     */
    @Transactional(rollbackFor = Exception.class)
    public LotTrace createLot(LotTrace lot) {
        // 若未指定批次号则自动生成 / Auto-generate lot number if not specified
        if (lot.getLotNo() == null || lot.getLotNo().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "批次号不能为空");
        }

        // 检查批次号唯一性 / Check lot number uniqueness
        LotTrace existing = lotTraceMapper.selectOne(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getLotNo, lot.getLotNo()));
        if (existing != null) {
            throw new BizException(ErrorCode.LOT_DUPLICATE.getCode(), "批次号重复: " + lot.getLotNo());
        }

        lot.setStatus(LotTrace.STATUS_RECEIVED);
        lotTraceMapper.insert(lot);
        log.info("批次已创建: lotNo={} material={} supplier={} qty={}",
                lot.getLotNo(), lot.getMaterialId(), lot.getSupplierId(), lot.getQuantity());
        return lot;
    }

    /**
     * 质检不合格锁定：按批次号快速锁定受影响范围 /
     * Quick lock on inspection failure: lock affected scope by lot number
     * <p>
     * 将该批次标记为 REJECTED，并返回同物料下的所有其他批次供风险评估。 /
     * Marks this lot as REJECTED and returns all other lots of the same material for risk assessment.
     *
     * @param lotNo  批次号 / Lot number
     * @param reason 不合格原因 / Rejection reason
     * @return 受影响范围 / Affected scope
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> lockLot(String lotNo, String reason) {
        LotTrace lot = lotTraceMapper.selectOne(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getLotNo, lotNo));

        if (lot == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getCode(), "批次不存在: " + lotNo);
        }

        // 标记不合格 / Mark as rejected
        lot.setStatus(LotTrace.STATUS_REJECTED);
        lot.setInspectionResult(LotTrace.INSPECTION_FAIL);
        lot.setInspectionDate(LocalDateTime.now());
        lot.setRejectionReason(reason);
        lotTraceMapper.updateById(lot);

        // 查询同物料所有批次供风险评估 / Query all lots of same material for risk assessment
        List<LotTrace> relatedLots = lotTraceMapper.selectList(
                new LambdaQueryWrapper<LotTrace>()
                        .eq(LotTrace::getMaterialId, lot.getMaterialId())
                        .ne(LotTrace::getId, lot.getId())
                        .orderByDesc(LotTrace::getReceiveDate));

        log.warn("批次已锁定(不合格): lotNo={} material={} reason={} relatedLots={}",
                lotNo, lot.getMaterialId(), reason, relatedLots.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lockedLot", lot);
        result.put("affectedMaterialId", lot.getMaterialId());
        result.put("relatedLots", relatedLots);
        result.put("riskAssessment", String.format(
                "物料 %d 在锁定批次外还有 %d 个关联批次，建议逐一核查",
                lot.getMaterialId(), relatedLots.size()));
        return result;
    }

    /**
     * 按日期范围分页查询批次 / Query lots by date range (paginated)
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @param status    状态过滤 / Status filter
     * @param page      当前页 / Current page
     * @param size      每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<LotTrace> listByDateRange(LocalDate startDate, LocalDate endDate, String status, int page, int size) {
        LambdaQueryWrapper<LotTrace> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(LotTrace::getReceiveDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(LotTrace::getReceiveDate, endDate);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(LotTrace::getStatus, status);
        }
        wrapper.orderByDesc(LotTrace::getReceiveDate);
        return lotTraceMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
