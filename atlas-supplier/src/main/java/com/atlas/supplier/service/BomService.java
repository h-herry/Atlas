package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.Bom;
import com.atlas.supplier.entity.BomItem;
import com.atlas.supplier.mapper.BomItemMapper;
import com.atlas.supplier.mapper.BomMapper;
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
 * BOM（物料清单）Service / BOM (Bill of Materials) Service
 *
 * <p>提供 BOM CRUD、发布/归档、版本管理、成本预估（∑物料单价×用量）。 /
 * Provides BOM CRUD, publish/archive, version management, and cost estimation (Σ unit price × quantity).</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BomService {

    private final BomMapper bomMapper;
    private final BomItemMapper bomItemMapper;

    /**
     * 创建 BOM / Create BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public Bom create(Bom bom) {
        // 版本唯一性校验 / Version uniqueness check
        Long count = bomMapper.selectCount(
                new LambdaQueryWrapper<Bom>()
                        .eq(Bom::getBomCode, bom.getBomCode())
                        .eq(Bom::getVersion, bom.getVersion()));
        if (count > 0) {
            throw new BizException(ErrorCode.BOM_VERSION_DUPLICATE);
        }
        bom.setStatus(0);
        bom.setCreatedAt(LocalDateTime.now());
        bom.setUpdatedAt(LocalDateTime.now());
        bomMapper.insert(bom);
        log.info("BOM创建成功: code={} version={}", bom.getBomCode(), bom.getVersion());
        return bom;
    }

    /**
     * 添加 BOM 明细 / Add BOM item
     */
    @Transactional(rollbackFor = Exception.class)
    public BomItem addItem(BomItem item) {
        Bom bom = bomMapper.selectById(item.getBomId());
        if (bom == null) {
            throw new BizException(ErrorCode.BOM_NOT_EXIST);
        }
        if (bom.getStatus() == 1) {
            throw new BizException(ErrorCode.BOM_ALREADY_PUBLISHED);
        }
        item.setCreatedAt(LocalDateTime.now());
        bomItemMapper.insert(item);
        return item;
    }

    /**
     * 删除 BOM 明细 / Remove BOM item
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(Long itemId) {
        BomItem item = bomItemMapper.selectById(itemId);
        if (item == null) {
            return;
        }
        Bom bom = bomMapper.selectById(item.getBomId());
        if (bom != null && bom.getStatus() == 1) {
            throw new BizException(ErrorCode.BOM_ALREADY_PUBLISHED);
        }
        bomItemMapper.deleteById(itemId);
    }

    /**
     * 查询 BOM 详情（含明细列表） / Query BOM detail (with item list)
     */
    public Bom getById(Long id) {
        Bom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BizException(ErrorCode.BOM_NOT_EXIST);
        }
        return bom;
    }

    /**
     * 查询 BOM 明细列表 / Query BOM item list
     */
    public List<BomItem> listItems(Long bomId) {
        return bomItemMapper.selectList(
                new LambdaQueryWrapper<BomItem>().eq(BomItem::getBomId, bomId));
    }

    /**
     * 分页查询 BOM / Paginated query of BOM
     */
    public Page<Bom> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Bom::getBomCode, keyword).or().like(Bom::getProductName, keyword));
        }
        if (status != null) {
            wrapper.eq(Bom::getStatus, status);
        }
        wrapper.orderByDesc(Bom::getCreatedAt);
        return bomMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 发布 BOM（状态由编辑中 → 已发布） / Publish BOM (draft → published)
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Bom bom = getById(id);
        if (bom.getStatus() == 1) {
            throw new BizException(ErrorCode.BOM_ALREADY_PUBLISHED);
        }
        bom.setStatus(1);
        bom.setUpdatedAt(LocalDateTime.now());
        bomMapper.updateById(bom);
        log.info("BOM发布成功: id={} code={}", id, bom.getBomCode());
    }

    /**
     * 归档 BOM（状态由已发布 → 已归档） / Archive BOM (published → archived)
     */
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long id) {
        Bom bom = getById(id);
        if (bom.getStatus() != 1) {
            throw new BizException(12007, "仅已发布状态的BOM可归档");
        }
        bom.setStatus(2);
        bom.setUpdatedAt(LocalDateTime.now());
        bomMapper.updateById(bom);
        log.info("BOM归档成功: id={} code={}", id, bom.getBomCode());
    }

    /**
     * 成本预估：∑物料单价 × 用量 × (1 + 损耗率) / Cost estimation: Σ unit price × quantity × (1 + scrap rate)
     *
     * <p>预留：实际物料单价需从 goods.default_price 获取。
     * 当前返回各明细的理论用量汇总（不含单价因子）。 /
     * Note: actual unit prices should be fetched from goods.default_price.
     * Currently returns theoretical quantity aggregation (without price factor).</p>
     *
     * @param bomId BOM ID
     * @return 成本预估 / Cost estimation (material base quantity aggregation)
     */
    public BigDecimal estimateCost(Long bomId) {
        List<BomItem> items = listItems(bomId);
        BigDecimal total = BigDecimal.ZERO;
        for (BomItem item : items) {
            // 有效用量 = 用量 × (1 + 损耗率/100) / Effective quantity = quantity × (1 + scrap rate / 100)
            BigDecimal effectiveQty = item.getQuantity().multiply(
                    BigDecimal.ONE.add(item.getScrapRate().divide(new BigDecimal("100"), 6,
                            java.math.RoundingMode.HALF_UP)));
            total = total.add(effectiveQty);
        }
        return total;
    }
}
