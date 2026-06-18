package com.atlas.supplier.controller.enterprise;

import com.alibaba.fastjson2.JSON;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.dto.portal.*;
import com.atlas.supplier.entity.PortalRegister;
import com.atlas.supplier.mapper.PortalRegisterMapper;
import com.atlas.supplier.service.portal.SupplierOnboardingWorkflowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 供应商入驻控制器（企业端） — 采购员代供应商注册 /
 * Supplier onboarding controller (enterprise side) — purchaser proxy supplier registration
 *
 * <p>需企业端登录，采购员代供应商提交入驻申请 /
 * Requires enterprise login, purchaser submits onboarding application on behalf of supplier</p>
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Slf4j
@RestController
@RequestMapping("/enterprise/supplier/onboarding")
@RequiredArgsConstructor
public class SupplierOnboardingController {

    private final PortalRegisterMapper portalRegisterMapper;
    private final SupplierOnboardingWorkflowService workflowService;

    /**
     * 采购员代供应商注册 — 需企业端登录 /
     * Purchaser proxy supplier registration — requires enterprise login
     *
     * <p>保存供应商入驻申请（标记 initiator），创建 Flowable 审批流程，
     * 系统自动发送短信/邮件通知供应商完善信息。 /
     * Save supplier onboarding application (marked with initiator), create Flowable approval process,
     * system auto-sends SMS/email to notify supplier to complete information.</p>
     */
    @PostMapping("/register")
    @RequirePermission("supplier:onboarding:add")
    public Result<RegisterSubmitResponse> register(@Valid @RequestBody PurchaserRegisterRequest request) {
        // 1. 检查是否重复提交 / Check for duplicate submission
        Long existingCount = portalRegisterMapper.selectCount(
                new LambdaQueryWrapper<PortalRegister>()
                        .eq(PortalRegister::getCreditCode, request.getCreditCode())
                        .in(PortalRegister::getApplyStatus, 0, 1)
        );
        if (existingCount > 0) {
            throw new BizException(ErrorCode.BIZ_EXCEPTION,
                    "该统一社会信用代码已有审批中的入驻申请，请勿重复提交 / A pending onboarding application with this credit code already exists");
        }

        // 2. 保存入驻申请（标记发起人） / Save application (mark initiator)
        PortalRegister application = buildApplication(request);
        application.setSource("PURCHASER");
        application.setInitiatorId(request.getInitiatorId());
        application.setInitiatorName(request.getInitiatorName());
        portalRegisterMapper.insert(application);

        // 3. 启动 Flowable 审批流程 / Start Flowable approval process
        String processInstanceId = workflowService.startApprovalProcess(application, "PURCHASER");

        // 4. 发送通知给供应商联系人 / Send notification to supplier contact
        workflowService.sendPurchaserProxyNotification(application);

        log.info("采购员代注册成功: applyId={}, companyName={}, initiator={}, processInstanceId={}",
                application.getId(), request.getCompanyName(), request.getInitiatorName(), processInstanceId);

        return Result.ok(RegisterSubmitResponse.builder()
                .applyId(application.getId())
                .processInstanceId(processInstanceId)
                .applyStatus(application.getApplyStatus())
                .message("入驻申请已提交，已通知供应商完善信息 / Application submitted, supplier has been notified to complete information")
                .build());
    }

    /**
     * 采购员查看自己发起的入驻申请列表 — 分页 /
     * Purchaser views their initiated onboarding applications — paginated
     *
     * @param initiatorId 采购员ID / Purchaser ID
     * @param applyStatus 申请状态（可选筛选） / Application status (optional filter)
     * @param page        页码 / Page number
     * @param size        每页条数 / Page size
     */
    @GetMapping("/list")
    @RequirePermission("supplier:onboarding:view")
    public Result<Page<PortalRegister>> list(@RequestParam Long initiatorId,
                                              @RequestParam(required = false) Integer applyStatus,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<PortalRegister> wrapper = new LambdaQueryWrapper<PortalRegister>()
                .eq(PortalRegister::getSource, "PURCHASER")
                .eq(PortalRegister::getInitiatorId, initiatorId)
                .orderByDesc(PortalRegister::getCreatedAt);

        if (applyStatus != null) {
            wrapper.eq(PortalRegister::getApplyStatus, applyStatus);
        }

        Page<PortalRegister> result = portalRegisterMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    /**
     * 查看入驻申请详情 / View onboarding application detail
     *
     * @param applyId 入驻申请ID / Application ID
     */
    @GetMapping("/{applyId}")
    @RequirePermission("supplier:onboarding:view")
    public Result<RegisterStatusResponse> detail(@PathVariable Long applyId) {
        RegisterStatusResponse status = workflowService.getApprovalProgress(applyId);
        return Result.ok(status);
    }

    // ==================== 内部辅助 / Internal Helpers ====================

    /**
     * 构建入驻申请实体 / Build onboarding application entity
     */
    private PortalRegister buildApplication(PurchaserRegisterRequest request) {
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
        app.setApplyStatus(0); // 待审批 / Pending

        if (request.getCertificates() != null && !request.getCertificates().isEmpty()) {
            app.setCertificates(JSON.toJSONString(request.getCertificates()));
        }

        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        return app;
    }
}
