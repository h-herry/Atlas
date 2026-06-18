package com.atlas.quality.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.quality.entity.NcrRecord;
import com.atlas.quality.mapper.NcrRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 不合格品处理（NCR）核心业务服务 / Non-Conformance Report (NCR) core business service
 * <p>
 * 创建 NCR → 处置决策 → 闭环。支持五种处置方式：让步接收/特采/挑选/退货/报废。 /
 * Create NCR → disposition decision → closure. Supports five dispositions: accept / concession / sort / return / scrap.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NcrService {

    private final NcrRecordMapper ncrMapper;

    /** 处置方式常量 / Disposition constants */
    public static final String DISP_ACCEPT = "ACCEPT";
    public static final String DISP_CONCESSION = "CONCESSION";
    public static final String DISP_SORT = "SORT";
    public static final String DISP_RETURN = "RETURN";
    public static final String DISP_SCRAP = "SCRAP";

    /** 缺陷严重程度 / Defect severity */
    public static final String SEVERITY_CRITICAL = "CRITICAL";
    public static final String SEVERITY_MAJOR = "MAJOR";
    public static final String SEVERITY_MINOR = "MINOR";

    /**
     * 创建 NCR 不合格品报告 / Create NCR report
     *
     * @param inspectId        关联检验单ID / Associated inspection ID
     * @param materialId       物料ID / Material ID
     * @param materialName     物料名称 / Material name
     * @param batchNo          批次号 / Batch number
     * @param supplierId       供应商ID / Supplier ID
     * @param defectType       缺陷类型 / Defect type
     * @param defectDescription 缺陷描述 / Defect description
     * @param defectQty        不合格数量 / Defect quantity
     * @param defectSeverity   严重程度 / Severity
     * @param createdBy        创建人 / Created by
     * @return NCR 记录 / NCR record
     */
    @Transactional(rollbackFor = Exception.class)
    public NcrRecord createNcr(Long inspectId, Long materialId, String materialName,
                                String batchNo, Long supplierId, String defectType,
                                String defectDescription, java.math.BigDecimal defectQty,
                                String defectSeverity, Long createdBy) {
        String ncrNo = "NCR" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        NcrRecord ncr = new NcrRecord();
        ncr.setNcrNo(ncrNo);
        ncr.setInspectId(inspectId);
        ncr.setMaterialId(materialId);
        ncr.setMaterialName(materialName);
        ncr.setBatchNo(batchNo);
        ncr.setSupplierId(supplierId);
        ncr.setDefectType(defectType);
        ncr.setDefectDescription(defectDescription);
        ncr.setDefectQty(defectQty);
        ncr.setDefectSeverity(defectSeverity != null ? defectSeverity : SEVERITY_MINOR);
        ncr.setClosed(0);
        ncr.setCreatedBy(createdBy);
        ncrMapper.insert(ncr);

        log.info("NCR 创建: ncrNo={} materialId={} defectQty={} severity={}",
                ncrNo, materialId, defectQty, defectSeverity);
        return ncr;
    }

    /**
     * 处置决策 / Disposition decision
     *
     * @param ncrId           NCR ID / NCR ID
     * @param disposition     处置方式 / Disposition
     * @param dispositionBy   处置人ID / Disposition by
     * @param dispositionReason 处置理由 / Disposition reason
     * @param correctiveAction 纠正措施 / Corrective action
     */
    @Transactional(rollbackFor = Exception.class)
    public void dispose(Long ncrId, String disposition, Long dispositionBy,
                         String dispositionReason, String correctiveAction) {
        NcrRecord ncr = ncrMapper.selectById(ncrId);
        if (ncr == null) {
            throw new BizException(6501, "NCR 记录不存在 / NCR record not found");
        }
        if (ncr.getDisposition() != null) {
            throw new BizException(6502, "该 NCR 已完成处置，不可重复处置 / This NCR has already been disposed");
        }

        ncr.setDisposition(disposition);
        ncr.setDispositionBy(dispositionBy);
        ncr.setDispositionAt(LocalDateTime.now());
        ncr.setDispositionReason(dispositionReason);
        ncr.setCorrectiveAction(correctiveAction);
        ncrMapper.updateById(ncr);

        log.info("NCR 处置完成: ncrNo={} disposition={} by={}", ncr.getNcrNo(), disposition, dispositionBy);
    }

    /**
     * 闭环 NCR / Close NCR
     *
     * @param ncrId NCR ID / NCR ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void close(Long ncrId) {
        NcrRecord ncr = ncrMapper.selectById(ncrId);
        if (ncr == null) {
            throw new BizException(6501, "NCR 记录不存在 / NCR record not found");
        }
        if (ncr.getDisposition() == null) {
            throw new BizException(6503, "请先完成处置决策再闭环 / Please complete disposition before closing");
        }
        ncr.setClosed(1);
        ncr.setClosedAt(LocalDateTime.now());
        ncrMapper.updateById(ncr);
        log.info("NCR 已闭环: ncrNo={}", ncr.getNcrNo());
    }

    /**
     * 按 ID 查询 / Query by ID
     */
    public NcrRecord getById(Long ncrId) {
        NcrRecord ncr = ncrMapper.selectById(ncrId);
        if (ncr == null) {
            throw new BizException(6501, "NCR 记录不存在 / NCR record not found");
        }
        return ncr;
    }

    /**
     * 分页查询 NCR（支持按物料/供应商/处置状态/闭环状态筛选） / Paginated query
     */
    public Page<NcrRecord> page(Long materialId, Long supplierId, String disposition, Integer closed,
                                  int page, int size) {
        LambdaQueryWrapper<NcrRecord> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(NcrRecord::getMaterialId, materialId);
        }
        if (supplierId != null) {
            wrapper.eq(NcrRecord::getSupplierId, supplierId);
        }
        if (disposition != null && !disposition.isEmpty()) {
            wrapper.eq(NcrRecord::getDisposition, disposition);
        }
        if (closed != null) {
            wrapper.eq(NcrRecord::getClosed, closed);
        }
        wrapper.orderByDesc(NcrRecord::getCreatedAt);
        return ncrMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询未闭环 NCR / Query open NCRs
     */
    public List<NcrRecord> listOpen() {
        return ncrMapper.selectList(
                new LambdaQueryWrapper<NcrRecord>()
                        .eq(NcrRecord::getClosed, 0)
                        .orderByAsc(NcrRecord::getCreatedAt));
    }
}
