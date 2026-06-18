package com.atlas.receipt.settlement.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.receipt.settlement.entity.ThreeWayMatch;
import com.atlas.receipt.settlement.service.ThreeWayMatchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 三单匹配 REST API / Three-way match REST API
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Tag(name = "三单匹配 / Three-Way Match")
public class MatchController {

    private final ThreeWayMatchService threeWayMatchService;

    /**
     * 执行三单匹配 / Execute three-way match
     * <p>
     * 匹配维度：PO 采购订单 vs 收货单 vs 发票 /
     * Match dimensions: PO vs Receipt vs Invoice
     */
    @PostMapping("/execute")
    @RequirePermission("match:execute")
    public Result<ThreeWayMatch> execute(@RequestParam @NotNull Long settlementId,
                                          @RequestParam @NotBlank String invoiceNo) {
        return Result.ok(threeWayMatchService.execute(settlementId, invoiceNo));
    }

    /**
     * 匹配详情 / Match detail
     */
    @GetMapping("/{id}")
    @RequirePermission("match:view")
    public Result<ThreeWayMatch> get(@PathVariable Long id) {
        return Result.ok(threeWayMatchService.getById(id));
    }
}
