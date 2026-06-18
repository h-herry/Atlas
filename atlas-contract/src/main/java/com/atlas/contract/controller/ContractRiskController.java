package com.atlas.contract.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.contract.entity.ContractRiskClause;
import com.atlas.contract.service.ContractRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 合同风险预警 Controller / Contract risk alert Controller
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/contract/risk")
@RequiredArgsConstructor
@Tag(name = "合同风险管理 / Contract Risk Management")
public class ContractRiskController {

    private final ContractRiskService riskService;

    @GetMapping("/{contractId}")
    @RequirePermission("contract:view")
    public Result<List<ContractRiskClause>> listByContractId(@PathVariable Long contractId) {
        return Result.ok(riskService.listByContractId(contractId));
    }

    @GetMapping("/level/{riskLevel}")
    @RequirePermission("contract:view")
    public Result<List<ContractRiskClause>> listByRiskLevel(@PathVariable String riskLevel) {
        return Result.ok(riskService.listByRiskLevel(riskLevel));
    }

    @PostMapping("/scan/{contractId}")
    @RequirePermission("contract:edit")
    public Result<Integer> scanRisk(@PathVariable Long contractId, @RequestBody String fullText) {
        return Result.ok(riskService.scanRisk(contractId, fullText));
    }

    @PostMapping
    @RequirePermission("contract:edit")
    public Result<Void> manualMark(@RequestParam Long contractId,
                                    @RequestParam String clauseContent,
                                    @RequestParam String riskType,
                                    @RequestParam String riskLevel,
                                    @RequestParam(required = false) String suggestion) {
        riskService.manualMark(contractId, clauseContent, riskType, riskLevel, suggestion);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @RequirePermission("contract:edit")
    public Result<Void> updateRiskLevel(@PathVariable Long id,
                                         @RequestParam String riskLevel,
                                         @RequestParam(required = false) String suggestion) {
        riskService.updateRiskLevel(id, riskLevel, suggestion);
        return Result.ok();
    }
}
