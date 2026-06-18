package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.ProductionProgress;
import com.atlas.supplier.mapper.ProductionProgressMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 供应商生产进度 Service / Supplier production progress Service
 *
 * <p>供应商填报生产进度，采购方可查看。 /
 * Supplier reports production progress; buyer can view.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionProgressService {

    private final ProductionProgressMapper progressMapper;

    /**
     * 供应商填报生产进度 / Supplier reports production progress
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionProgress report(ProductionProgress progress) {
        // 自动计算进度百分比 / Auto-calculate progress percentage
        if (progress.getTotalQty() != null && progress.getProducedQty() != null
                && progress.getTotalQty().compareTo(BigDecimal.ZERO) > 0) {
            progress.setProgressPercent(
                    progress.getProducedQty()
                            .multiply(new BigDecimal("100"))
                            .divide(progress.getTotalQty(), 2, RoundingMode.HALF_UP));
        }
        progress.setReportTime(LocalDateTime.now());
        progress.setCreatedAt(LocalDateTime.now());
        progressMapper.insert(progress);
        log.info("生产进度填报: orderId={} materialId={} progress={}%",
                progress.getPurchaseOrderId(), progress.getMaterialId(), progress.getProgressPercent());
        return progress;
    }

    /**
     * 更新生产进度 / Update production progress
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionProgress update(ProductionProgress progress) {
        ProductionProgress existing = progressMapper.selectById(progress.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.PROGRESS_NOT_EXIST);
        }
        if (progress.getTotalQty() != null && progress.getProducedQty() != null
                && progress.getTotalQty().compareTo(BigDecimal.ZERO) > 0) {
            progress.setProgressPercent(
                    progress.getProducedQty()
                            .multiply(new BigDecimal("100"))
                            .divide(progress.getTotalQty(), 2, RoundingMode.HALF_UP));
        }
        progress.setReportTime(LocalDateTime.now());
        progressMapper.updateById(progress);
        return progressMapper.selectById(progress.getId());
    }

    /**
     * 按采购订单查询最新进度 / Query latest progress by purchase order
     */
    public ProductionProgress getByOrder(Long orderId, Long materialId) {
        return progressMapper.selectOne(
                new LambdaQueryWrapper<ProductionProgress>()
                        .eq(ProductionProgress::getPurchaseOrderId, orderId)
                        .eq(ProductionProgress::getMaterialId, materialId)
                        .orderByDesc(ProductionProgress::getReportTime)
                        .last("LIMIT 1"));
    }

    /**
     * 分页查询生产进度 / Paginated query of production progress
     */
    public Page<ProductionProgress> page(Long orderId, Long supplierId, int page, int size) {
        LambdaQueryWrapper<ProductionProgress> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(ProductionProgress::getPurchaseOrderId, orderId);
        }
        if (supplierId != null) {
            wrapper.eq(ProductionProgress::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(ProductionProgress::getReportTime);
        return progressMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
