package com.atlas.purchase.inquiry.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.inquiry.entity.Quote;
import com.atlas.purchase.inquiry.entity.QuoteItem;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 多维比价引擎 / Multi-dimension quote comparison engine
 *
 * <p>综合四个维度自动评分排序：价格(50%)、交期(20%)、质量资质(15%)、历史绩效(15%)。 /
 * Auto-scoring and ranking across four dimensions:
 * price (50%), delivery (20%), quality (15%), historical performance (15%).
 * 权重可通过 application.yml 中的 inquiry.score.weights 配置。 /
 * Weights configurable via inquiry.score.weights in application.yml.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteComparisonService {

    private final QuoteService quoteService;

    // 默认权重 / Default weights (overridable via application.yml)
    private double priceWeight = 50.0;
    private double deliveryWeight = 20.0;
    private double qualityWeight = 15.0;
    private double historyWeight = 15.0;

    /**
     * 多维比价打分 / Multi-dimension comparison scoring
     *
     * @param inquiryId 询价单ID / Inquiry ID
     * @return 按综合得分降序排列的报价比较结果 / Comparison results sorted by composite score descending
     */
    public List<Map<String, Object>> compare(Long inquiryId) {
        List<Quote> quotes = quoteService.listByInquiry(inquiryId);
        if (quotes.isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "该询价单元报价 / No quotes for this inquiry");
        }

        // 收集各项指标用于归一化 / Collect metrics for normalization
        List<BigDecimal> allPrices = new ArrayList<>();
        List<Integer> allDeliveryDays = new ArrayList<>();
        for (Quote q : quotes) {
            List<QuoteItem> items = quoteService.listItems(q.getId());
            allPrices.add(calcAveragePrice(items));
            allDeliveryDays.add(calcMaxDeliveryDays(items));
        }

        BigDecimal maxPrice = allPrices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        BigDecimal minPrice = allPrices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        double maxDelivery = allDeliveryDays.stream().max(Integer::compareTo).orElse(1);
        double minDelivery = allDeliveryDays.stream().min(Integer::compareTo).orElse(0);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Quote q : quotes) {
            List<QuoteItem> items = quoteService.listItems(q.getId());
            BigDecimal avgPrice = calcAveragePrice(items);
            int maxDD = calcMaxDeliveryDays(items);

            // 价格得分：最低价=满分，线性插值（0-100归一化） / Price: lowest=100, linear interpolation
            double priceScore = normalizePrice(avgPrice, minPrice, maxPrice) * (priceWeight / 100.0) * 100.0;

            // 交期得分：最短交期=满分 / Delivery: shortest=100
            double deliveryScore = normalizeDelivery(maxDD, minDelivery, maxDelivery) * (deliveryWeight / 100.0) * 100.0;

            // 质量得分：默认 80（后续可对接资质系统） / Quality: default 80 (integrable with certification system)
            double qualityScore = 80.0 * (qualityWeight / 100.0);

            // 历史绩效得分：默认 70（后续可对接供应商绩效系统） / History: default 70 (integrable with performance system)
            double historyScore = 70.0 * (historyWeight / 100.0);

            double composite = priceScore + deliveryScore + qualityScore + historyScore;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("quoteId", q.getId());
            entry.put("supplierId", q.getSupplierId());
            entry.put("totalAmount", q.getTotalAmount());
            entry.put("avgPrice", avgPrice.setScale(2, RoundingMode.HALF_UP));
            entry.put("maxDeliveryDays", maxDD);
            entry.put("priceScore", Math.round(priceScore * 100.0) / 100.0);
            entry.put("deliveryScore", Math.round(deliveryScore * 100.0) / 100.0);
            entry.put("qualityScore", Math.round(qualityScore * 100.0) / 100.0);
            entry.put("historyScore", Math.round(historyScore * 100.0) / 100.0);
            entry.put("compositeScore", Math.round(composite * 100.0) / 100.0);
            results.add(entry);
        }

        results.sort((a, b) -> Double.compare(
            (Double) b.get("compositeScore"), (Double) a.get("compositeScore")));

        log.info("比价完成: inquiryId={} quoteCount={}", inquiryId, results.size());
        return results;
    }

    /**
     * 计算报价平均单价 / Calculate average unit price
     */
    private BigDecimal calcAveragePrice(List<QuoteItem> items) {
        if (items.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = items.stream()
            .map(QuoteItem::getUnitPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(items.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算最大承诺交期 / Calculate max committed delivery days
     */
    private int calcMaxDeliveryDays(List<QuoteItem> items) {
        return items.stream()
            .mapToInt(QuoteItem::getDeliveryDays)
            .max()
            .orElse(0);
    }

    /**
     * 价格归一化（最低价=1.0，最高价=0.0） / Price normalization (lowest=1.0, highest=0.0)
     */
    private double normalizePrice(BigDecimal price, BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) == 0) return 1.0;
        return max.subtract(price).divide(max.subtract(min), 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 交期归一化（最短交期=1.0，最长=0.0） / Delivery normalization (shortest=1.0, longest=0.0)
     */
    private double normalizeDelivery(int days, double min, double max) {
        if (min == max) return 1.0;
        return (max - days) / (max - min);
    }
}
