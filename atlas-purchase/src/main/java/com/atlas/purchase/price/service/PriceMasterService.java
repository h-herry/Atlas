package com.atlas.purchase.price.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.price.entity.PriceMaster;
import com.atlas.purchase.price.mapper.PriceMasterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 价格主数据业务服务 / Price master data business service
 *
 * <p>管理物料-供应商维度的标准化价格主数据，支持有效期管理、来源追溯（MANUAL/SETTLEMENT/CONTRACT）和历史价格查询。 /
 * Manages standardized material-supplier price master data with validity management,
 * source traceability (MANUAL/SETTLEMENT/CONTRACT), and historical price queries.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceMasterService extends ServiceImpl<PriceMasterMapper, PriceMaster> {

    private final PriceMasterMapper priceMasterMapper;

    /**
     * 新增价格 / Add new price
     *
     * <p>同一物料-供应商如有有效价格，自动失效旧记录。 /
     * If an active price exists for the same material-supplier, auto-invalidates the old record.</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public PriceMaster add(PriceMaster price) {
        // 失效同一物料-供应商的旧有效价格 / Invalidate old active price for same material-supplier
        List<PriceMaster> existing = listActive(price.getMaterialId(), price.getSupplierId());
        for (PriceMaster old : existing) {
            old.setStatus(0);
            updateById(old);
            log.info("旧价格已失效: id={}", old.getId());
        }

        if (price.getStatus() == null) {
            price.setStatus(1);
        }
        if (price.getCurrency() == null) {
            price.setCurrency("CNY");
        }
        if (price.getSource() == null) {
            price.setSource("MANUAL");
        }
        save(price);
        log.info("价格主数据新增: materialId={} supplierId={} unitPrice={}", price.getMaterialId(), price.getSupplierId(), price.getUnitPrice());
        return price;
    }

    /**
     * 更新价格 / Update price
     */
    @Transactional(rollbackFor = Exception.class)
    public PriceMaster update(Long id, PriceMaster updated) {
        PriceMaster existing = getById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        updated.setId(id);
        updateById(updated);
        log.info("价格主数据更新: id={}", id);
        return getById(id);
    }

    /**
     * 查询当前有效价格 / Query current active price
     */
    public Optional<PriceMaster> getActivePrice(Long materialId, Long supplierId) {
        List<PriceMaster> list = listActive(materialId, supplierId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        // 返回有效期内的最新价格 / Return the latest price within validity
        LocalDate today = LocalDate.now();
        return list.stream()
            .filter(p -> p.getEffectiveDate() == null || !p.getEffectiveDate().isAfter(today))
            .filter(p -> p.getExpireDate() == null || !p.getExpireDate().isBefore(today))
            .findFirst();
    }

    /**
     * 历史价格趋势 / Historical price trend
     */
    public List<PriceMaster> getHistory(Long materialId) {
        return priceMasterMapper.selectList(
            new LambdaQueryWrapper<PriceMaster>()
                .eq(PriceMaster::getMaterialId, materialId)
                .orderByDesc(PriceMaster::getCreatedAt)
        );
    }

    /**
     * 按来源查询价格 / Query prices by source
     */
    public List<PriceMaster> listBySource(Long materialId, String source) {
        return priceMasterMapper.selectList(
            new LambdaQueryWrapper<PriceMaster>()
                .eq(PriceMaster::getMaterialId, materialId)
                .eq(PriceMaster::getSource, source)
                .eq(PriceMaster::getStatus, 1)
                .orderByDesc(PriceMaster::getCreatedAt)
        );
    }

    /**
     * 失效价格 / Invalidate price
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidate(Long id) {
        PriceMaster price = getById(id);
        if (price == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        price.setStatus(0);
        updateById(price);
        log.info("价格已失效: id={}", id);
    }

    private List<PriceMaster> listActive(Long materialId, Long supplierId) {
        return priceMasterMapper.selectList(
            new LambdaQueryWrapper<PriceMaster>()
                .eq(PriceMaster::getMaterialId, materialId)
                .eq(PriceMaster::getSupplierId, supplierId)
                .eq(PriceMaster::getStatus, 1)
        );
    }
}
