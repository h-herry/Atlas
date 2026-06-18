package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MaterialTrace;
import com.atlas.supplier.mapper.MaterialTraceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料全链路追溯 Service / Material full-chain traceability Service
 *
 * <p>录入追溯记录 + 按批次号/条码全链路查询。 /
 * Record trace entries + full-chain query by batch number / barcode.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialTraceService {

    private final MaterialTraceMapper traceMapper;

    /**
     * 写入追溯记录 / Write trace record
     *
     * @param materialId  物料ID / Material ID
     * @param batchNo     批次号 / Batch number
     * @param barcode     条码 / Barcode
     * @param traceType   追溯类型: RECEIVE/ISSUE/PRODUCE/INSPECT/RETURN / Trace type
     * @param sourceId    来源单据ID / Source document ID
     * @param sourceNo    来源单据号 / Source document number
     * @param quantity    变动数量 / Change quantity
     * @param warehouseId 库位ID / Warehouse ID
     * @param operatorId  操作人ID / Operator ID
     * @return 追溯记录 / Trace record
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialTrace record(Long materialId, String batchNo, String barcode,
                                 String traceType, Long sourceId, String sourceNo,
                                 BigDecimal quantity, Long warehouseId, Long operatorId) {
        MaterialTrace trace = new MaterialTrace();
        trace.setTraceNo("TR" + System.currentTimeMillis());
        trace.setMaterialId(materialId);
        trace.setBatchNo(batchNo);
        trace.setBarcode(barcode);
        trace.setTraceType(traceType);
        trace.setSourceId(sourceId);
        trace.setSourceNo(sourceNo);
        trace.setQuantity(quantity);
        trace.setWarehouseId(warehouseId);
        trace.setOperatorId(operatorId);
        trace.setOperatedAt(LocalDateTime.now());
        trace.setCreatedAt(LocalDateTime.now());
        traceMapper.insert(trace);
        return trace;
    }

    /**
     * 按批次号全链路追溯（入库→质检→出库→生产→退货完整链路） /
     * Full-chain trace by batch number (inbound → inspection → outbound → production → return)
     */
    public List<MaterialTrace> traceByBatch(String batchNo) {
        return traceMapper.selectList(
                new LambdaQueryWrapper<MaterialTrace>()
                        .eq(MaterialTrace::getBatchNo, batchNo)
                        .orderByAsc(MaterialTrace::getOperatedAt));
    }

    /**
     * 按条码全链路追溯 / Full-chain trace by barcode
     */
    public List<MaterialTrace> traceByBarcode(String barcode) {
        return traceMapper.selectList(
                new LambdaQueryWrapper<MaterialTrace>()
                        .eq(MaterialTrace::getBarcode, barcode)
                        .orderByAsc(MaterialTrace::getOperatedAt));
    }

    /**
     * 按物料ID + 批次号查询 / Query by material ID + batch number
     */
    public List<MaterialTrace> traceByMaterialAndBatch(Long materialId, String batchNo) {
        return traceMapper.selectList(
                new LambdaQueryWrapper<MaterialTrace>()
                        .eq(MaterialTrace::getMaterialId, materialId)
                        .eq(MaterialTrace::getBatchNo, batchNo)
                        .orderByAsc(MaterialTrace::getOperatedAt));
    }

    /**
     * 分页查询追溯记录 / Paginated query of trace records
     */
    public Page<MaterialTrace> page(Long materialId, String batchNo, String traceType, int page, int size) {
        LambdaQueryWrapper<MaterialTrace> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(MaterialTrace::getMaterialId, materialId);
        }
        if (batchNo != null) {
            wrapper.eq(MaterialTrace::getBatchNo, batchNo);
        }
        if (traceType != null) {
            wrapper.eq(MaterialTrace::getTraceType, traceType);
        }
        wrapper.orderByDesc(MaterialTrace::getOperatedAt);
        return traceMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
