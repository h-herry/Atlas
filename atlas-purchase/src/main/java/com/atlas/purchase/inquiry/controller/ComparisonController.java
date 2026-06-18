package com.atlas.purchase.inquiry.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.inquiry.service.QuoteComparisonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 比价引擎 REST API / Comparison engine REST API
 *
 * <p>多维自动打分：价格(50%) + 交期(20%) + 质量(15%) + 历史(15%)。 /
 * Multi-dimension auto-scoring: price(50%) + delivery(20%) + quality(15%) + history(15%).</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
@Tag(name = "多维比价 / Quote Comparison")
public class ComparisonController {

    private final QuoteComparisonService comparisonService;

    /**
     * 多维比价打分 / Multi-dimension comparison scoring
     */
    @GetMapping("/{inquiryId}")
    @RequirePermission("purchase:view")
    public Result<List<Map<String, Object>>> compare(@PathVariable Long inquiryId) {
        return Result.ok(comparisonService.compare(inquiryId));
    }
}
