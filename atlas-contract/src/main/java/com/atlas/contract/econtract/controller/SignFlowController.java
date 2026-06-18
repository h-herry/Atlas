package com.atlas.contract.econtract.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.contract.econtract.dto.SignFlowCreateRequest;
import com.atlas.contract.econtract.dto.SignFlowStatusResponse;
import com.atlas.contract.econtract.model.CntSignRecord;
import com.atlas.contract.econtract.service.SignFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 签署流程管理 Controller / Sign flow management Controller
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/contract/sign")
@RequiredArgsConstructor
public class SignFlowController {

    private final SignFlowService signFlowService;
    private final JwtUtil jwtUtil;

    // ============ 发起签署流程 / Initiate Sign Flow ============

    /**
     * 发起签署流程 / Initiate sign flow
     */
    @PostMapping("/flow")
    @RequirePermission("contract:sign:initiate")
    @AuditLog(module = "CONTRACT_SIGN", operation = "CREATE", description = "发起签署流程")
    public Result<Long> createFlow(@Valid @RequestBody SignFlowCreateRequest request,
                                    @RequestHeader("Authorization") String authHeader) {
        Long initiatorId = extractUserId(authHeader);
        Long flowId = signFlowService.createFlow(request, initiatorId);
        return Result.ok(flowId);
    }

    // ============ 查看签署进度 / View Sign Progress ============

    /**
     * 查看签署进度 / View sign progress
     */
    @GetMapping("/flow/{id}")
    @RequirePermission("contract:sign:view")
    public Result<SignFlowStatusResponse> getFlow(@PathVariable Long id) {
        return Result.ok(signFlowService.getFlowStatus(id));
    }

    // ============ 在线签署 / Online Sign ============

    /**
     * 在线签署 / Online sign
     */
    @PostMapping("/flow/{id}/sign")
    @RequirePermission("contract:sign:execute")
    @AuditLog(module = "CONTRACT_SIGN", operation = "SIGN", description = "在线签署")
    public Result<String> sign(@PathVariable Long id,
                                @RequestBody Map<String, String> body,
                                @RequestHeader("Authorization") String authHeader) {
        Long signerId = extractUserId(authHeader);
        String signIp = body.getOrDefault("signIp", "127.0.0.1");
        String signMethod = body.getOrDefault("signMethod", "SMS_VERIFY");
        signFlowService.sign(id, signerId, signIp, signMethod);
        return Result.ok("签署成功");
    }

    // ============ 拒绝签署 / Reject Sign ============

    /**
     * 拒绝签署 / Reject sign
     */
    @PostMapping("/flow/{id}/reject")
    @RequirePermission("contract:sign:execute")
    @AuditLog(module = "CONTRACT_SIGN", operation = "REJECT", description = "拒绝签署")
    public Result<String> reject(@PathVariable Long id,
                                  @RequestBody Map<String, String> body,
                                  @RequestHeader("Authorization") String authHeader) {
        Long signerId = extractUserId(authHeader);
        String reason = body.getOrDefault("reason", "未提供原因");
        signFlowService.reject(id, signerId, reason);
        return Result.ok("已拒绝签署");
    }

    // ============ 查询签署记录 / Query Sign Records ============

    /**
     * 查询签署记录 / Query sign records
     */
    @GetMapping("/records/{contractId}")
    @RequirePermission("contract:sign:view")
    public Result<List<CntSignRecord>> listRecords(@PathVariable Long contractId) {
        return Result.ok(signFlowService.listRecordsByContractId(contractId));
    }

    // ============ Token 工具方法 / Token Utility Methods ============

    private Long extractUserId(String header) {
        String token = header.substring(7);
        return jwtUtil.getUserId(jwtUtil.parseToken(token));
    }
}
