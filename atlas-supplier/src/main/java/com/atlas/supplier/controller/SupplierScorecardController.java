package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.SupplierScorecard;
import com.atlas.supplier.entity.SupplierScorecardItem;
import com.atlas.supplier.service.SupplierScorecardService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商绩效评分卡 Controller — REST API for supplier performance scorecard /
 * 供应商绩效评分卡 Controller — 供应商绩效评分卡管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier/scorecard")
@RequiredArgsConstructor
@Tag(name = "供应商绩效评分卡 / Supplier Scorecard")
public class SupplierScorecardController {

    private final SupplierScorecardService scorecardService;

    /**
     * 创建评分卡 / Create scorecard
     *
     * <p>请求体包含主表字段和 items 明细项列表，Service 自动汇总各维度得分并计算综合评级。 /
     * Request body includes master fields and items list, Service auto-aggregates scores and computes grade.</p>
     */
    @PostMapping
    @Operation(summary = "创建评分卡 / Create scorecard")
    @ApiOperationSupport(order = 1)
    public Result<SupplierScorecard> create(@RequestBody Map<String, Object> body) {
        // 解析主表和明细 / Parse master and items
        SupplierScorecard scorecard = new SupplierScorecard();
        scorecard.setSupplierId(Long.valueOf(body.get("supplierId").toString()));
        scorecard.setPeriod((String) body.get("period"));
        scorecard.setAssessorId(Long.valueOf(body.get("assessorId").toString()));
        scorecard.setAssessorName((String) body.get("assessorName"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) body.get("items");
        List<SupplierScorecardItem> items = itemsRaw.stream().map(m -> {
            SupplierScorecardItem item = new SupplierScorecardItem();
            item.setDimension((String) m.get("dimension"));
            item.setItemName((String) m.get("itemName"));
            item.setMaxScore(new java.math.BigDecimal(m.get("maxScore").toString()));
            item.setActualScore(new java.math.BigDecimal(m.get("actualScore").toString()));
            return item;
        }).toList();

        return Result.success(scorecardService.create(scorecard, items));
    }

    /**
     * 按供应商+周期查询 / Query by supplier + period
     */
    @GetMapping("/query")
    @Operation(summary = "按供应商+周期查询评分卡 / Query scorecard by supplier + period")
    @ApiOperationSupport(order = 2)
    public Result<List<SupplierScorecard>> query(@RequestParam Long supplierId,
                                                  @RequestParam(required = false) String period) {
        return Result.success(scorecardService.query(supplierId, period));
    }

    /**
     * 查询评分卡明细项 / Query scorecard line items
     */
    @GetMapping("/{id}/items")
    @Operation(summary = "查询评分卡明细项 / Query scorecard line items")
    @ApiOperationSupport(order = 3)
    public Result<List<SupplierScorecardItem>> items(@PathVariable Long id) {
        return Result.success(scorecardService.listItems(id));
    }

    /**
     * 查询评分卡详情 / Query scorecard detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询评分卡详情 / Query scorecard detail")
    @ApiOperationSupport(order = 4)
    public Result<SupplierScorecard> detail(@PathVariable Long id) {
        return Result.success(scorecardService.getById(id));
    }

    /**
     * 发布评分卡 / Publish scorecard
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布评分卡 / Publish scorecard")
    @ApiOperationSupport(order = 5)
    public Result<Void> publish(@PathVariable Long id) {
        scorecardService.publish(id);
        return Result.success();
    }
}
