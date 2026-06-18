package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.IqcInspection;
import com.atlas.supplier.entity.MaterialTrace;
import com.atlas.supplier.entity.SupplierDelivery;
import com.atlas.supplier.mapper.IqcInspectionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 来料检验 IQC Service / Incoming Quality Control (IQC) Service
 *
 * <p>收货触发检验任务 → 检验判定 → 合格/退货/返工/让步接收 → 写入追溯记录。 /
 * Receipt triggers inspection task → judgment → PASS/REJECT/REWORK/ACCEPT → write trace record.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IqcInspectionService {

    private final IqcInspectionMapper inspectionMapper;
    private final MaterialTraceService traceService;

    /**
     * 收货后自动创建 IQC 检验任务（由 SupplierDeliveryService 收货后联动调用） /
     * Auto-create IQC inspection task upon receipt (linked call from SupplierDeliveryService)
     */
    @Transactional(rollbackFor = Exception.class)
    public IqcInspection createInspection(SupplierDelivery delivery, Long materialId,
                                           String batchNo, java.math.BigDecimal qty) {
        IqcInspection inspection = new IqcInspection();
        inspection.setInspectionNo("IQC" + System.currentTimeMillis());
        inspection.setDeliveryId(delivery.getId());
        inspection.setMaterialId(materialId);
        inspection.setBatchNo(batchNo);
        inspection.setInspectionQty(qty);
        inspection.setCreatedAt(LocalDateTime.now());
        inspection.setUpdatedAt(LocalDateTime.now());
        inspectionMapper.insert(inspection);
        log.info("IQC检验任务创建: inspectionNo={} deliveryId={}", inspection.getInspectionNo(), delivery.getId());
        return inspection;
    }

    /**
     * 执行检验判定 / Execute inspection judgment
     *
     * @param inspectionId 检验单ID / Inspection ID
     * @param result       判定结果: PASS/REJECT/REWORK/ACCEPT
     * @param qualifiedQty 合格数量 / Qualified quantity
     * @param defectiveQty 不合格数量 / Defective quantity
     * @param inspectorId  检验员ID / Inspector ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void inspect(Long inspectionId, String result,
                         java.math.BigDecimal qualifiedQty, java.math.BigDecimal defectiveQty,
                         Long inspectorId) {
        IqcInspection inspection = inspectionMapper.selectById(inspectionId);
        if (inspection == null) {
            throw new BizException(ErrorCode.INSPECTION_NOT_EXIST);
        }
        if (StrUtil.isNotBlank(inspection.getResult())) {
            throw new BizException(ErrorCode.INSPECTION_ALREADY_DONE);
        }
        inspection.setResult(result);
        inspection.setQualifiedQty(qualifiedQty);
        inspection.setDefectiveQty(defectiveQty);
        inspection.setInspectorId(inspectorId);
        inspection.setInspectedAt(LocalDateTime.now());
        inspection.setUpdatedAt(LocalDateTime.now());
        inspectionMapper.updateById(inspection);

        // 写入追溯记录 / Write trace record
        traceService.record(inspection.getMaterialId(), inspection.getBatchNo(), null,
                "INSPECT", inspection.getId(), inspection.getInspectionNo(),
                qualifiedQty, null, inspectorId);

        log.info("IQC检验完成: inspectionNo={} result={}", inspection.getInspectionNo(), result);
    }

    /**
     * 查询检验单 / Query inspection record
     */
    public IqcInspection getById(Long id) {
        IqcInspection inspection = inspectionMapper.selectById(id);
        if (inspection == null) {
            throw new BizException(ErrorCode.INSPECTION_NOT_EXIST);
        }
        return inspection;
    }

    /**
     * 分页查询检验单 / Paginated query of inspection records
     */
    public Page<IqcInspection> page(String keyword, Long deliveryId, String result, int page, int size) {
        LambdaQueryWrapper<IqcInspection> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(IqcInspection::getInspectionNo, keyword);
        }
        if (deliveryId != null) {
            wrapper.eq(IqcInspection::getDeliveryId, deliveryId);
        }
        if (StrUtil.isNotBlank(result)) {
            wrapper.eq(IqcInspection::getResult, result);
        }
        wrapper.orderByDesc(IqcInspection::getCreatedAt);
        return inspectionMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
