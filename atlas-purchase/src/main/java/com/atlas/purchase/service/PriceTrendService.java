package com.atlas.purchase.service;

import com.atlas.purchase.entity.PriceLibrary;
import com.atlas.purchase.entity.PriceTrend;
import com.atlas.purchase.mapper.PriceLibraryMapper;
import com.atlas.purchase.mapper.PriceTrendMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史价格趋势 Service — 按物料+供应商查询历史成交价 / 月度均价统计 /
 * Historical price trend Service — query historical transaction prices by material+supplier / monthly average statistics
 *
 * <p>从价格库(PriceLibrary)中统计指定物料和供应商在指定月数内的月度均价、
 * 最低价、最高价，输出价格走势数据，支持价格波动预警。 /
 * Aggregates monthly average/min/max prices from PriceLibrary for specified material and supplier
 * within specified months range, supports price volatility alerts.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceTrendService {

    private final PriceLibraryMapper priceLibraryMapper;
    private final PriceTrendMapper priceTrendMapper;

    /**
     * 按物料+供应商查询月度均价趋势 / Query monthly average price trend by material + supplier
     *
     * @param materialId 物料ID / Material ID
     * @param supplierId 供应商ID（可选，不传则查该物料所有供应商） / Supplier ID (optional, all suppliers if null)
     * @param months     查询月数 / Number of months to query
     * @return 月度价格趋势数据列表 / Monthly price trend data list
     */
    public List<Map<String, Object>> queryTrend(Long materialId, Long supplierId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        // 从价格库查询历史数据 / Query historical data from price library
        LambdaQueryWrapper<PriceLibrary> wrapper = new LambdaQueryWrapper<PriceLibrary>()
            .eq(PriceLibrary::getMaterialId, materialId)
            .eq(PriceLibrary::getStatus, 1)
            .ge(PriceLibrary::getValidFrom, startDate.toString())
            .orderByAsc(PriceLibrary::getValidFrom);

        if (supplierId != null) {
            wrapper.eq(PriceLibrary::getSupplierId, supplierId);
        }

        List<PriceLibrary> prices = priceLibraryMapper.selectList(wrapper);

        // 按月份分组统计 / Group by month and aggregate
        Map<String, List<BigDecimal>> monthlyMap = new LinkedHashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (PriceLibrary price : prices) {
            String monthKey = price.getValidFrom() != null
                ? price.getValidFrom().format(monthFormatter)
                : price.getCreatedAt().toLocalDate().format(monthFormatter);
            monthlyMap.computeIfAbsent(monthKey, k -> new ArrayList<>())
                .add(price.getUnitPrice());
        }

        // 生成月度均价结果 / Generate monthly average results
        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal prevAvg = null;

        for (Map.Entry<String, List<BigDecimal>> entry : monthlyMap.entrySet()) {
            Map<String, Object> monthData = new LinkedHashMap<>();
            List<BigDecimal> monthPrices = entry.getValue();
            monthData.put("period", entry.getKey());
            monthData.put("avgPrice", monthPrices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(monthPrices.size()), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP));
            monthData.put("minPrice", monthPrices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            monthData.put("maxPrice", monthPrices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            monthData.put("transactionCount", monthPrices.size());

            // 走势方向 / Trend direction
            BigDecimal currentAvg = (BigDecimal) monthData.get("avgPrice");
            if (prevAvg == null) {
                monthData.put("trendDirection", "STABLE");
            } else {
                int cmp = currentAvg.compareTo(prevAvg);
                monthData.put("trendDirection", cmp > 0 ? "RISE" : cmp < 0 ? "FALL" : "STABLE");
            }
            prevAvg = currentAvg;
            result.add(monthData);
        }

        log.info("价格趋势查询: materialId={}, supplierId={}, months={}, 月度数据={}",
            materialId, supplierId, months, result.size());
        return result;
    }

    /**
     * 获取物料最新价格 / Get latest price for material
     *
     * @param materialId 物料ID / Material ID
     * @return 最新价格信息 / Latest price info (price, supplierId, validFrom)
     */
    public Map<String, Object> getLatestPrice(Long materialId) {
        PriceLibrary latest = priceLibraryMapper.selectOne(
            new LambdaQueryWrapper<PriceLibrary>()
                .eq(PriceLibrary::getMaterialId, materialId)
                .eq(PriceLibrary::getStatus, 1)
                .orderByDesc(PriceLibrary::getValidFrom)
                .last("LIMIT 1"));

        if (latest == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("materialId", materialId);
        result.put("supplierId", latest.getSupplierId());
        result.put("unitPrice", latest.getUnitPrice());
        result.put("validFrom", latest.getValidFrom());
        result.put("priceType", latest.getPriceType());
        return result;
    }

    /**
     * 查询已存在的价格趋势统计记录 / Query existing price trend statistics
     *
     * @param materialId 物料ID / Material ID
     * @param months     查询月数 / Number of months
     * @return 趋势记录列表 / Trend record list
     */
    public List<PriceTrend> listTrendRecords(Long materialId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        String startPeriod = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return priceTrendMapper.selectList(
            new LambdaQueryWrapper<PriceTrend>()
                .eq(PriceTrend::getMaterialId, materialId)
                .ge(PriceTrend::getPeriod, startPeriod)
                .orderByAsc(PriceTrend::getPeriod));
    }
}
