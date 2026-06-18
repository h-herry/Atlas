package com.atlas.contract.econtract.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.contract.econtract.dto.ClauseDiffResponse;
import com.atlas.contract.econtract.dto.PerformanceCreateRequest;
import com.atlas.contract.econtract.model.CntPerformance;
import com.atlas.contract.econtract.model.CntPerformanceAlert;
import com.atlas.contract.econtract.service.ClauseCompareService;
import com.atlas.contract.econtract.service.ContractPerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 履约管理与条款比对 Controller / Performance management & clause comparison Controller
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
@Tag(name = "合同履约管理 / Contract Performance Management")
public class ContractPerformanceController {

    private final ContractPerformanceService performanceService;
    private final ClauseCompareService clauseCompareService;
    private final JwtUtil jwtUtil;

    // ============ 履约跟踪 / Performance Tracking ============

    /**
     * 创建履约指标 / Create performance metric
     */
    @PostMapping("/performance")
    @RequirePermission("contract:performance:add")
    @AuditLog(module = "CONTRACT_PERFORMANCE", operation = "CREATE", description = "创建履约指标")
    public Result<Long> create(@Valid @RequestBody PerformanceCreateRequest request) {
        return Result.ok(performanceService.create(request));
    }

    /**
     * 查询履约列表 / Query performance list
     */
    @GetMapping("/performance/list/{contractId}")
    @RequirePermission("contract:performance:view")
    public Result<List<CntPerformance>> list(@PathVariable Long contractId) {
        return Result.ok(performanceService.listByContractId(contractId));
    }

    /**
     * 更新履约进度 / Update performance progress
     */
    @PutMapping("/performance/{id}")
    @RequirePermission("contract:performance:edit")
    @AuditLog(module = "CONTRACT_PERFORMANCE", operation = "UPDATE", description = "更新履约进度")
    public Result<String> updateProgress(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String actualValue = body.get("actualValue");
        String remark = body.get("remark");
        performanceService.updateProgress(id, status, actualValue, remark);
        return Result.ok("更新成功");
    }

    /**
     * 查询违约预警 / Query breach alerts
     */
    @GetMapping("/performance/alerts/{contractId}")
    @RequirePermission("contract:performance:view")
    public Result<List<CntPerformanceAlert>> listAlerts(@PathVariable Long contractId) {
        return Result.ok(performanceService.listAlertsByContractId(contractId));
    }

    // ============ 条款比对 / Clause Comparison ============

    /**
     * 条款版本比对 / Clause version comparison
     */
    @PostMapping("/clause/compare")
    @RequirePermission("contract:clause:compare")
    @AuditLog(module = "CONTRACT_CLAUSE", operation = "COMPARE", description = "条款版本比对")
    public Result<ClauseDiffResponse> compare(@RequestBody Map<String, String> body,
                                               @RequestHeader("Authorization") String authHeader) {
        Long contractId = Long.valueOf(body.get("contractId"));
        String sourceVersion = body.get("sourceVersion");
        String targetVersion = body.get("targetVersion");
        String sourceText = body.get("sourceText");
        String targetText = body.get("targetText");
        String comparedBy = extractRealName(authHeader);
        return Result.ok(clauseCompareService.compare(
                contractId, sourceVersion, targetVersion, sourceText, targetText, comparedBy));
    }

    // ============ Token 工具方法 / Token Utility Methods ============

    private String extractRealName(String header) {
        String token = header.substring(7);
        return String.valueOf(jwtUtil.parseToken(token).get("realName", String.class));
    }
}
