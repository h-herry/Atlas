package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.ConsultationReview;
import com.atlas.purchase.entity.ConsultationSession;
import com.atlas.purchase.service.ConsultationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 竞争性磋商 REST API / Competitive consultation REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@Tag(name = "竞争性磋商管理 / Consultation Management")
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * 创建磋商会话（草稿） / Create consultation session (draft)
     */
    @PostMapping("/session")
    @RequirePermission("consultation:add")
    public Result<ConsultationSession> createSession(@RequestBody ConsultationSession session) {
        return Result.ok(consultationService.createSession(session));
    }

    /**
     * 进入公告期 / Enter announcement period
     */
    @PostMapping("/session/{sessionId}/announce")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> announce(@PathVariable Long sessionId) {
        return Result.ok(consultationService.announce(sessionId));
    }

    /**
     * 开启响应文件提交 / Open response document submission
     */
    @PostMapping("/session/{sessionId}/response")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> openResponse(@PathVariable Long sessionId) {
        return Result.ok(consultationService.openResponse(sessionId));
    }

    /**
     * 进入磋商阶段 / Enter consultation phase
     */
    @PostMapping("/session/{sessionId}/start")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> startConsultation(@PathVariable Long sessionId) {
        return Result.ok(consultationService.startConsultation(sessionId));
    }

    /**
     * 触发最终报价 / Trigger final bid
     */
    @PostMapping("/session/{sessionId}/final-offer")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> finalOffer(@PathVariable Long sessionId) {
        return Result.ok(consultationService.finalOffer(sessionId));
    }

    /**
     * 提交评审记录 / Submit review record
     */
    @PostMapping("/review")
    @RequirePermission("consultation:manage")
    public Result<ConsultationReview> submitReview(@RequestBody ConsultationReview review) {
        return Result.ok(consultationService.submitReview(review));
    }

    /**
     * 综合评审判定标 / Comprehensive evaluation and award
     */
    @PostMapping("/session/{sessionId}/award")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> award(@PathVariable Long sessionId) {
        return Result.ok(consultationService.award(sessionId));
    }

    /**
     * 终止磋商 / Terminate consultation
     */
    @PostMapping("/session/{sessionId}/terminate")
    @RequirePermission("consultation:manage")
    public Result<ConsultationSession> terminate(@PathVariable Long sessionId) {
        return Result.ok(consultationService.terminate(sessionId));
    }

    /**
     * 分页查询磋商会话 / Paginated query of consultation sessions
     */
    @GetMapping("/session/page")
    @RequirePermission("consultation:view")
    public Result<PageResult<ConsultationSession>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ConsultationSession> result = consultationService.page(keyword, status, page, size);
        return PageResult.ok(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 查询磋商会话详情 / Query consultation session detail
     */
    @GetMapping("/session/{sessionId}")
    @RequirePermission("consultation:view")
    public Result<ConsultationSession> getSession(@PathVariable Long sessionId) {
        return Result.ok(consultationService.getSession(sessionId));
    }

    /**
     * 查询评审记录列表 / Query review record list
     */
    @GetMapping("/session/{sessionId}/reviews")
    @RequirePermission("consultation:view")
    public Result<List<ConsultationReview>> listReviews(@PathVariable Long sessionId) {
        return Result.ok(consultationService.listReviews(sessionId));
    }
}
