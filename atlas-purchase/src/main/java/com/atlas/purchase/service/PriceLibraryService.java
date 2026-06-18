package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.PriceLibrary;
import com.atlas.purchase.entity.PriceTrend;
import com.atlas.purchase.entity.SupplierRecommendation;
import com.atlas.purchase.mapper.PriceLibraryMapper;
import com.atlas.purchase.mapper.PriceTrendMapper;
import com.atlas.purchase.mapper.SupplierRecommendationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 价格库服务 — 价格录入/查询/作废、自动比价、价格走势分析与供应商智能推荐
 * Price library service — price entry/query/invalidation, auto price comparison,
 * price trend analysis, and smart supplier recommendation
 * <p>
 * 支持四种价格类型 / Supports four price types:
 * CONTRACT（合同价）/ CONTRACT (contract price),
 * QUOTATION（报价）/ QUOTATION (quotation),
 * SPOT（现货价）/ SPOT (spot price),
 * AGREEMENT（协议价）/ AGREEMENT (agreement price).
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceLibraryService extends ServiceImpl<PriceLibraryMapper, PriceLibrary> {

    private final PriceLibraryMapper priceMapper;
    private final PriceTrendMapper trendMapper;
    private final SupplierRecommendationMapper recommendationMapper;

    // ==================== 价格库 CRUD / Price Library CRUD ====================

    /**
     * 分页查询价格记录 — 支持按物料/供应商/价格类型筛选
     * Paginated query of price records — filterable by material, supplier, and price type
     *
     * @param materialId 物料ID（可选） / material ID (optional)
     * @param supplierId 供应商ID（可选） / supplier ID (optional)
     * @param priceType  价格类型（可选） / price type (optional)
     * @param page       当前页码 / current page number
     * @param size       每页大小 / page size
     * @return 分页结果 / paginated result
     */
    public Page<PriceLibrary> page(Long materialId, Long supplierId, String priceType, int page, int size) {
        LambdaQueryWrapper<PriceLibrary> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) wrapper.eq(PriceLibrary::getMaterialId, materialId);
        if (supplierId != null) wrapper.eq(PriceLibrary::getSupplierId, supplierId);
        if (priceType != null) wrapper.eq(PriceLibrary::getPriceType, priceType);
        wrapper.eq(PriceLibrary::getStatus, 1).orderByDesc(PriceLibrary::getCreatedAt);
        return priceMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 录入价格 — 新增一条有效价格记录，默认状态为有效（1）
     * Add a price entry — create a new price record with default status active (1)
     *
     * @param price 价格库实体 / price library entity
     * @return true-录入成功 / true if added successfully
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addPrice(PriceLibrary price) {
        price.setStatus(1);
        return save(price);
    }

    /**
     * 作废价格 — 逻辑删除，将状态置为无效（0）
     * Invalidate a price — soft delete by setting status to 0
     *
     * @param id 价格记录ID / price record ID
     * @return true-作废成功 / true if invalidated successfully
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean invalidatePrice(Long id) {
        PriceLibrary price = new PriceLibrary();
        price.setId(id);
        price.setStatus(0);
        return updateById(price);
    }

    // ==================== 自动比价 / Auto Price Comparison ====================

    /**
     * 自动比价 — 查询指定物料当前有效的最低价格记录
     * Auto price comparison — find the current lowest active price for a given material
     *
     * @param materialId 物料ID / material ID
     * @return 最低价格记录（Optional，无有效价格时为空） / lowest price record (Optional, empty if no active price)
     */
    public Optional<PriceLibrary> compareLowestPrice(Long materialId) {
        LambdaQueryWrapper<PriceLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceLibrary::getMaterialId, materialId)
               .eq(PriceLibrary::getStatus, 1)
               .apply("valid_to IS NULL OR valid_to >= {0}", LocalDate.now())
               .orderByAsc(PriceLibrary::getUnitPrice);
        List<PriceLibrary> list = priceMapper.selectList(wrapper);
        return list.stream().findFirst();
    }

    /**
     * 查询指定物料的所有供应商报价（按价格升序排列）
     * List all supplier prices for a given material (sorted by price ascending)
     *
     * @param materialId 物料ID / material ID
     * @return 价格列表（按单价升序） / price list (sorted by unit price ascending)
     */
    public List<PriceLibrary> listByMaterialSorted(Long materialId) {
        LambdaQueryWrapper<PriceLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceLibrary::getMaterialId, materialId)
               .eq(PriceLibrary::getStatus, 1)
               .orderByAsc(PriceLibrary::getUnitPrice);
        return priceMapper.selectList(wrapper);
    }

    // ==================== 价格走势分析 / Price Trend Analysis ====================

    /**
     * 查询物料价格走势 — 按统计周期升序排列
     * Query price trend for a material — sorted by period ascending
     *
     * @param materialId 物料ID / material ID
     * @return 价格走势列表 / price trend list
     */
    public List<PriceTrend> getTrend(Long materialId) {
        LambdaQueryWrapper<PriceTrend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceTrend::getMaterialId, materialId)
               .orderByAsc(PriceTrend::getPeriod);
        return trendMapper.selectList(wrapper);
    }

    /**
     * 重新计算某物料某月的价格走势 — 统计均价/最低价/最高价，并与上月对比判断趋势方向（涨/跌/稳）
     * Recalculate the price trend for a given material and period — compute average/min/max price,
     * compare with the previous period to determine trend direction (RISE/FALL/STABLE)
     *
     * @param materialId 物料ID / material ID
     * @param period     统计周期（格式 YYYY-MM） / statistical period (format YYYY-MM)
     */
    @Transactional(rollbackFor = Exception.class)
    public void recalculateTrend(Long materialId, String period) {
        // 查询该物料所有有效价格 / Query all active prices for the material
        LambdaQueryWrapper<PriceLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceLibrary::getMaterialId, materialId)
               .eq(PriceLibrary::getStatus, 1);
        List<PriceLibrary> prices = priceMapper.selectList(wrapper);

        if (prices.isEmpty()) return;

        // 计算均价 / Calculate average price
        BigDecimal avg = prices.stream()
                .map(PriceLibrary::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(prices.size()), 4, RoundingMode.HALF_UP);

        // 计算最低价 / Calculate minimum price
        BigDecimal min = prices.stream().map(PriceLibrary::getUnitPrice).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        // 计算最高价 / Calculate maximum price
        BigDecimal max = prices.stream().map(PriceLibrary::getUnitPrice).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        // 与上月对比判断趋势方向 / Compare with previous period to determine trend direction
        String prevPeriod = computePrevPeriod(period);
        PriceTrend prevTrend = trendMapper.selectOne(
            new LambdaQueryWrapper<PriceTrend>()
                .eq(PriceTrend::getMaterialId, materialId)
                .eq(PriceTrend::getPeriod, prevPeriod)
        );

        String direction;
        if (prevTrend == null || prevTrend.getAvgPrice() == null) {
            direction = "STABLE";
        } else {
            int cmp = avg.compareTo(prevTrend.getAvgPrice());
            direction = cmp > 0 ? "RISE" : (cmp < 0 ? "FALL" : "STABLE");
        }

        // Upsert：存在则更新，不存在则插入 / Upsert: update if exists, insert otherwise
        PriceTrend trend = trendMapper.selectOne(
            new LambdaQueryWrapper<PriceTrend>()
                .eq(PriceTrend::getMaterialId, materialId)
                .eq(PriceTrend::getPeriod, period)
        );
        if (trend == null) {
            trend = new PriceTrend();
            trend.setMaterialId(materialId);
            trend.setPeriod(period);
        }
        trend.setAvgPrice(avg);
        trend.setMinPrice(min);
        trend.setMaxPrice(max);
        trend.setTransactionCount(prices.size());
        trend.setTrendDirection(direction);
        if (trend.getId() == null) {
            trendMapper.insert(trend);
        } else {
            trendMapper.updateById(trend);
        }
    }

    /**
     * 计算上一统计周期（格式 YYYY-MM）
     * Compute the previous statistical period (format YYYY-MM)
     *
     * @param period 当前周期 / current period
     * @return 上一周期字符串 / previous period string
     */
    private String computePrevPeriod(String period) {
        String[] parts = period.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        if (month == 1) return (year - 1) + "-12";
        return year + "-" + String.format("%02d", month - 1);
    }

    // ==================== 供应商智能推荐 / Smart Supplier Recommendation ====================

    /**
     * 按物料品类智能推荐供应商 — 按匹配分数降序排列
     * Smart supplier recommendation by material category — sorted by match score descending
     *
     * @param materialId 物料ID / material ID
     * @return 推荐供应商列表（按匹配分数降序） / recommended supplier list (sorted by match score descending)
     */
    public List<SupplierRecommendation> recommendSuppliers(Long materialId) {
        LambdaQueryWrapper<SupplierRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierRecommendation::getMaterialId, materialId)
               .orderByDesc(SupplierRecommendation::getMatchScore);
        return recommendationMapper.selectList(wrapper);
    }

    /**
     * 添加供应商推荐记录 / Add a supplier recommendation record
     *
     * @param rec 推荐记录实体 / recommendation record entity
     * @return true-添加成功 / true if added successfully
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addRecommendation(SupplierRecommendation rec) {
        return recommendationMapper.insert(rec) > 0;
    }
}
