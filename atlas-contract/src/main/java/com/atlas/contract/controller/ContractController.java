package com.atlas.contract.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.contract.entity.Contract;
import com.atlas.contract.service.ContractService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final JwtUtil jwtUtil;

    // ============ 基础 CRUD / Basic CRUD ============

    @GetMapping("/page")
    @RequirePermission("contract:view")
    public Result<Page<Contract>> page(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.ok(contractService.page(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @RequirePermission("contract:view")
    public Result<Contract> get(@PathVariable Long id) {
        return Result.ok(contractService.getById(id));
    }

    @PostMapping
    @RequirePermission("contract:add")
    public Result<Void> add(@Valid @RequestBody Contract contract) {
        contractService.save(contract);
        return Result.ok();
    }

    @PutMapping
    @RequirePermission("contract:edit")
    public Result<Void> update(@Valid @RequestBody Contract contract) {
        contractService.update(contract);
        return Result.ok();
    }

    // ============ 状态机操作 / State Machine Operations ============

    /**
     * 状态流转: 提交 / 审核 / 签署 / 执行 等 /
     * State transition: submit / review / sign / execute etc.
     */
    @PutMapping("/{id}/status/{targetStatus}")
    @RequirePermission("contract:audit")
    @AuditLog(module = "CONTRACT", operation = "APPROVE", description = "合同状态流转")
    public Result<String> transition(@PathVariable Long id,
                                     @PathVariable int targetStatus,
                                     @RequestHeader("Authorization") String authHeader) {
        Long operatorId = extractUserId(authHeader);
        String operatorName = extractRealName(authHeader);
        contractService.transition(id, targetStatus, operatorId, operatorName);
        return Result.ok("状态流转成功");
    }

    /**
     * 驳回 / Reject
     */
    @PutMapping("/{id}/reject")
    @RequirePermission("contract:audit")
    public Result<String> reject(@PathVariable Long id,
                                 @RequestBody Map<String, String> body,
                                 @RequestHeader("Authorization") String authHeader) {
        String reason = body.getOrDefault("reason", "未提供理由");
        Long operatorId = extractUserId(authHeader);
        String operatorName = extractRealName(authHeader);
        contractService.reject(id, reason, operatorId, operatorName);
        return Result.ok("已驳回");
    }

    private Long extractUserId(String header) {
        String token = header.substring(7);
        return jwtUtil.getUserId(jwtUtil.parseToken(token));
    }

    private String extractRealName(String header) {
        String token = header.substring(7);
        return String.valueOf(jwtUtil.parseToken(token).get("realName", String.class));
    }
}
