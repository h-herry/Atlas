package com.atlas.quality.controller;

import com.atlas.common.core.web.Result;
import com.atlas.quality.service.SupplierPpmService;
import com.atlas.quality.service.SupplierPpmService.PpmResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 供应商质量 PPM 控制器 / Supplier quality PPM controller
 * <p>
 * 提供单供应商 PPM 查询、PPM 排名、月度趋势。 /
 * Provides single supplier PPM query, PPM ranking, and monthly trend.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/quality/ppm")
@RequiredArgsConstructor
public class SupplierPpmController {

    private final SupplierPpmService ppmService;

    /**
     * 指定时间范围内供应商 PPM / Supplier PPM within time range
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param startDate  开始日期 / Start date
     * @param endDate    结束日期 / End date
     */
    @GetMapping("/{supplierId}")
    public Result<PpmResult> calculate(
            @PathVariable Long supplierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(ppmService.calculate(supplierId, startDate, endDate));
    }

    /**
     * 供应商 PPM 排名 / Supplier PPM ranking
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @param topN      返回前 N 名 / Top N (default 20)
     */
    @GetMapping("/ranking")
    public Result<List<PpmResult>> ranking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "20") int topN) {
        return Result.success(ppmService.ranking(startDate, endDate, topN));
    }

    /**
     * 月维度 PPM 趋势 / Monthly PPM trend
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param months     最近 N 个月 / Last N months (default 12)
     */
    @GetMapping("/trend/{supplierId}")
    public Result<List<PpmResult>> monthlyTrend(
            @PathVariable Long supplierId,
            @RequestParam(defaultValue = "12") int months) {
        return Result.success(ppmService.monthlyTrend(supplierId, months));
    }
}
