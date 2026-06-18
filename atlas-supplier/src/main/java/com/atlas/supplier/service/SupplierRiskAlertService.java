package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierRiskAlert;
import com.atlas.supplier.mapper.SupplierRiskAlertMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商风险预警 Service — 风险录入 / 风险查询 / 标记解决 /
 * Supplier risk alert Service — risk entry / risk query / mark resolved
 *
 * <p>支持多来源风险数据：企查查(QICHACHA) / 天眼查(TIANYANCHA) / 人工录入(MANUAL)，
 * 覆盖工商变更、司法风险、经营异常、财务风险四大类型。 /
 * Supports multi-source risk data from QICHACHA / TIANYANCHA / MANUAL entry,
 * covering business change, judicial, operational anomaly, and financial risk types.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierRiskAlertService {

    private final SupplierRiskAlertMapper riskAlertMapper;

    // ==================== 风险录入 / Risk Entry ====================

    /**
     * 录入风险预警 / Create risk alert
     *
     * @param alert 风险预警对象 / Risk alert object
     * @return 创建后的预警 / Created alert
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierRiskAlert create(SupplierRiskAlert alert) {
        if (alert.getAlertTime() == null) {
            alert.setAlertTime(LocalDateTime.now());
        }
        alert.setIsResolved(0);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        riskAlertMapper.insert(alert);
        log.info("录入风险预警: supplierId={}, riskType={}, level={}",
            alert.getSupplierId(), alert.getRiskType(), alert.getRiskLevel());
        return alert;
    }

    /**
     * 批量导入风险数据 / Batch import risk data
     *
     * @param alerts 风险预警列表 / Risk alert list
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<SupplierRiskAlert> alerts) {
        for (SupplierRiskAlert alert : alerts) {
            create(alert);
        }
    }

    // ==================== 风险查询 / Risk Query ====================

    /**
     * 按供应商查询全部风险预警 / Query all risk alerts for a supplier
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 风险预警列表（按预警时间降序） / Risk alert list (by alert time desc)
     */
    public List<SupplierRiskAlert> listBySupplierId(Long supplierId) {
        return riskAlertMapper.selectList(
            new LambdaQueryWrapper<SupplierRiskAlert>()
                .eq(SupplierRiskAlert::getSupplierId, supplierId)
                .orderByDesc(SupplierRiskAlert::getAlertTime));
    }

    /**
     * 按风险等级查询 / Query by risk level
     *
     * @param riskLevel 风险等级: LOW/MEDIUM/HIGH/CRITICAL / Risk level
     * @return 匹配的风险预警列表 / Matching risk alert list
     */
    public List<SupplierRiskAlert> listByRiskLevel(String riskLevel) {
        return riskAlertMapper.selectList(
            new LambdaQueryWrapper<SupplierRiskAlert>()
                .eq(SupplierRiskAlert::getRiskLevel, riskLevel)
                .eq(SupplierRiskAlert::getIsResolved, 0)
                .orderByDesc(SupplierRiskAlert::getAlertTime));
    }

    /**
     * 查询未解决的高危/严重风险 / Query unresolved high/critical risks
     *
     * @return 未解决的高风险预警列表 / Unresolved high-risk alert list
     */
    public List<SupplierRiskAlert> listUnresolvedHighRisk() {
        return riskAlertMapper.selectList(
            new LambdaQueryWrapper<SupplierRiskAlert>()
                .eq(SupplierRiskAlert::getIsResolved, 0)
                .in(SupplierRiskAlert::getRiskLevel, List.of("HIGH", "CRITICAL"))
                .orderByDesc(SupplierRiskAlert::getAlertTime));
    }

    /**
     * 按风险类型查询 / Query by risk type
     */
    public List<SupplierRiskAlert> listByRiskType(String riskType) {
        return riskAlertMapper.selectList(
            new LambdaQueryWrapper<SupplierRiskAlert>()
                .eq(SupplierRiskAlert::getRiskType, riskType)
                .eq(SupplierRiskAlert::getIsResolved, 0)
                .orderByDesc(SupplierRiskAlert::getAlertTime));
    }

    // ==================== 标记解决 / Mark Resolved ====================

    /**
     * 标记风险已解决 / Mark risk as resolved
     *
     * @param id          预警ID / Alert ID
     * @param handlerId   处理人ID / Handler ID
     * @param handlerName 处理人姓名 / Handler name
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id, Long handlerId, String handlerName) {
        SupplierRiskAlert alert = riskAlertMapper.selectById(id);
        if (alert == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "风险预警不存在: " + id);
        }
        if (alert.getIsResolved() == 1) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "该风险预警已解决");
        }
        riskAlertMapper.update(null,
            new LambdaUpdateWrapper<SupplierRiskAlert>()
                .eq(SupplierRiskAlert::getId, id)
                .set(SupplierRiskAlert::getIsResolved, 1)
                .set(SupplierRiskAlert::getResolvedTime, LocalDateTime.now())
                .set(SupplierRiskAlert::getHandlerId, handlerId)
                .set(SupplierRiskAlert::getHandlerName, handlerName)
                .set(SupplierRiskAlert::getUpdatedAt, LocalDateTime.now()));
        log.info("风险预警已解决: id={}, handler={}", id, handlerName);
    }

    /**
     * 查询风险预警详情 / Query risk alert detail
     */
    public SupplierRiskAlert getById(Long id) {
        SupplierRiskAlert alert = riskAlertMapper.selectById(id);
        if (alert == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "风险预警不存在: " + id);
        }
        return alert;
    }
}
