package com.atlas.supplier.controller.portal;

import com.alibaba.fastjson2.JSON;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.web.Result;
import com.atlas.supplier.dto.portal.*;
import com.atlas.supplier.entity.PortalRegister;
import com.atlas.supplier.mapper.PortalRegisterMapper;
import com.atlas.supplier.service.portal.PortalAuthService;
import com.atlas.supplier.service.portal.SupplierOnboardingWorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供应商认证控制器 — 供应商登录/Token获取/退出/刷新 + 自助注册 /
 * Supplier authentication controller — supplier login / token acquisition / logout / refresh + self-registration
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/portal/auth")
@RequiredArgsConstructor
@Tag(name = "门户认证 / Portal Auth")
public class PortalAuthController {

    private final PortalAuthService portalAuthService;
    private final PortalRegisterMapper portalRegisterMapper;
    private final SupplierOnboardingWorkflowService workflowService;

    // ==================== 登录/退出/刷新 / Login / Logout / Refresh ====================

    /**
     * 供应商登录 — 手机号/账号 + 密码 → JWT Token /
     * Supplier login — phone/account + password → JWT token
     */
    @PostMapping("/login")
    public Result<SupplierTokenResponse> login(@Valid @RequestBody SupplierLoginRequest request) {
        return Result.ok(portalAuthService.login(request));
    }

    /**
     * 供应商退出登录 — Token 加入黑名单（Redis 标记） /
     * Supplier logout — add token to blacklist (Redis mark)
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        portalAuthService.logout(token);
        return Result.ok();
    }

    /**
     * 刷新 Token — 使用 Refresh Token 换取新 Access Token /
     * Refresh token — exchange refresh token for new access token
     */
    @GetMapping("/refresh")
    public Result<SupplierTokenResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractToken(request);
        return Result.ok(portalAuthService.refresh(refreshToken));
    }

    // ==================== 供应商自助注册 / Supplier Self-Registration ====================

    /**
     * 供应商自助注册 — 匿名访问，无需登录 /
     * Supplier self-registration — anonymous access, no login required
     *
     * <p>提交公司信息 + 资质证书 → 创建 Flowable 审批流程 → 返回申请ID和流程实例ID /
     * Submit company info + certificates → create Flowable approval process → return application ID and process instance ID</p>
     */
    @PostMapping("/register")
    public Result<RegisterSubmitResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 1. 检查是否重复提交（同信用代码已有审批中的申请） / Check for duplicate submission (same credit code with pending application)
        Long existingCount = portalRegisterMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PortalRegister>()
                        .eq(PortalRegister::getCreditCode, request.getCreditCode())
                        .in(PortalRegister::getApplyStatus, 0, 1) // 待审批或审批中 / Pending or in review
        );
        if (existingCount > 0) {
            throw new BizException(ErrorCode.BIZ_EXCEPTION,
                    "该统一社会信用代码已有审批中的入驻申请，请勿重复提交 / A pending onboarding application with this credit code already exists");
        }

        // 2. 保存入驻申请 / Save onboarding application
        PortalRegister application = buildApplication(request, "SELF");
        portalRegisterMapper.insert(application);

        // 3. 启动 Flowable 审批流程 / Start Flowable approval process
        String processInstanceId = workflowService.startApprovalProcess(application, "SELF");

        log.info("供应商自助注册成功: applyId={}, companyName={}, processInstanceId={}",
                application.getId(), request.getCompanyName(), processInstanceId);

        return Result.ok(RegisterSubmitResponse.builder()
                .applyId(application.getId())
                .processInstanceId(processInstanceId)
                .applyStatus(application.getApplyStatus())
                .message("入驻申请已提交，请耐心等待审核 / Application submitted, please wait for review")
                .build());
    }

    /**
     * 查询入驻进度 — 匿名访问 /
     * Query registration progress — anonymous access
     *
     * @param applyId 入驻申请ID / Application ID
     * @return 审批进度 / Approval progress
     */
    @GetMapping("/register/{applyId}/status")
    public Result<RegisterStatusResponse> getRegisterStatus(@PathVariable Long applyId) {
        RegisterStatusResponse status = workflowService.getApprovalProgress(applyId);
        return Result.ok(status);
    }

    // ==================== 内部辅助方法 / Internal Helpers ====================

    /**
     * 构建入驻申请实体 / Build onboarding application entity
     *
     * @param request 注册请求 / Registration request
     * @param source  来源：SELF / PURCHASER / Channel: SELF / PURCHASER
     * @return 入驻申请实体 / Onboarding application entity
     */
    private PortalRegister buildApplication(RegisterRequest request, String source) {
        PortalRegister app = new PortalRegister();
        app.setCompanyName(request.getCompanyName());
        app.setCreditCode(request.getCreditCode());
        app.setLegalPerson(request.getLegalPerson());
        app.setContactName(request.getContactName());
        app.setContactPhone(request.getContactPhone());
        app.setContactEmail(request.getContactEmail());
        app.setIndustryCategory(request.getIndustryCategory());
        app.setMainProducts(request.getMainProducts());
        app.setAnnualRevenue(request.getAnnualRevenue());
        app.setEmployeeCount(request.getEmployeeCount());
        app.setSource(source);
        app.setApplyStatus(0); // 待审批 / Pending

        // 序列化资质证书列表为 JSON / Serialize certificate list to JSON
        if (request.getCertificates() != null && !request.getCertificates().isEmpty()) {
            app.setCertificates(JSON.toJSONString(request.getCertificates()));
        }

        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        return app;
    }

    /**
     * 从请求头提取 Bearer Token / Extract Bearer token from request header
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
