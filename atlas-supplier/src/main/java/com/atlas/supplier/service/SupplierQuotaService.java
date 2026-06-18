package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.SupplierQuota;
import com.atlas.supplier.mapper.SupplierQuotaMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商配额管理 Service / Supplier quota management Service
 *
 * <p>支持按物料品类配置供应商配额比例，CRUD + 按评级/绩效自动分配。
 * 配额总和不得超过 100%。 /
 * Supports configuring supplier quota percentages by material category with CRUD + auto-assignment by rating/performance.
 * Total quota must not exceed 100%.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierQuotaService extends ServiceImpl<SupplierQuotaMapper, SupplierQuota> {

    private final SupplierQuotaMapper quotaMapper;

    /**
     * 分页查询配额 / Paginated query of quotas
     */
    public Page<SupplierQuota> page(Long supplierId, int page, int size) {
        LambdaQueryWrapper<SupplierQuota> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierQuota::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(SupplierQuota::getCreatedAt);
        return quotaMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 按供应商查询配额列表 / List quotas by supplier
     */
    public List<SupplierQuota> listBySupplierId(Long supplierId) {
        LambdaQueryWrapper<SupplierQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierQuota::getSupplierId, supplierId)
               .eq(SupplierQuota::getQuotaStatus, 1);
        return quotaMapper.selectList(wrapper);
    }

    /**
     * 按品类和供应商查询配额 / Query quota by category and supplier
     */
    public SupplierQuota getBySupplierAndCategory(Long supplierId, Long categoryId) {
        LambdaQueryWrapper<SupplierQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierQuota::getSupplierId, supplierId)
               .eq(SupplierQuota::getMaterialCategoryId, categoryId)
               .eq(SupplierQuota::getQuotaStatus, 1);
        return quotaMapper.selectOne(wrapper);
    }

    /**
     * 新增配额 — 校验同品类配额总和不超过100% /
     * Add quota — validate that sum of quotas for the same category does not exceed 100%
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierQuota addQuota(SupplierQuota quota) {
        validateQuotaSum(quota);
        save(quota);
        return quota;
    }

    /**
     * 更新配额 / Update quota
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierQuota updateQuota(SupplierQuota quota) {
        SupplierQuota existing = getById(quota.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.QUOTA_NOT_EXIST);
        }
        if (!existing.getMaterialCategoryId().equals(quota.getMaterialCategoryId())) {
            validateQuotaSum(quota);
        }
        updateById(quota);
        return quota;
    }

    /**
     * 删除配额（逻辑失效） / Delete quota (soft disable)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuota(Long id) {
        SupplierQuota quota = new SupplierQuota();
        quota.setId(id);
        quota.setQuotaStatus(0);
        return updateById(quota);
    }

    /**
     * 批量更新配额状态 / Batch update quota status
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(java.util.List<Long> ids, Integer quotaStatus) {
        for (Long id : ids) {
            SupplierQuota quota = new SupplierQuota();
            quota.setId(id);
            quota.setQuotaStatus(quotaStatus);
            updateById(quota);
        }
        log.info("批量更新配额状态完成: ids={}, quotaStatus={}", ids.size(), quotaStatus);
    }

    /**
     * 校验同品类所有生效配额总和不超过 100 /
     * Validate that sum of all active quotas for the same category does not exceed 100
     */
    private void validateQuotaSum(SupplierQuota newQuota) {
        LambdaQueryWrapper<SupplierQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierQuota::getMaterialCategoryId, newQuota.getMaterialCategoryId())
               .eq(SupplierQuota::getQuotaStatus, 1);
        if (newQuota.getId() != null) {
            wrapper.ne(SupplierQuota::getId, newQuota.getId());
        }
        List<SupplierQuota> existingList = quotaMapper.selectList(wrapper);
        BigDecimal existingSum = existingList.stream()
                .map(q -> q.getQuotaPercent() != null ? q.getQuotaPercent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = existingSum.add(newQuota.getQuotaPercent() != null ? newQuota.getQuotaPercent() : BigDecimal.ZERO);
        if (total.compareTo(new BigDecimal("100")) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "同品类配额总和不能超过100%，当前已累计: " + total + "%");
        }
    }
}
