package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.NegotiationRound;
import com.atlas.purchase.entity.NegotiationSession;
import com.atlas.purchase.service.NegotiationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 竞争性谈判 REST API / Competitive negotiation REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/negotiation")
@RequiredArgsConstructor
public class NegotiationController {

    private final NegotiationService negotiationService;

    /**
     * 创建谈判会话（草稿） / Create negotiation session (draft)
     */
    @PostMapping("/session")
    @RequirePermission("negotiation:add")
    public Result<NegotiationSession> createSession(@RequestBody NegotiationSession session) {
        return Result.ok(negotiationService.createSession(session));
    }

    /**
     * 发布谈判 / Publish negotiation
     */
    @PostMapping("/session/{sessionId}/publish")
    @RequirePermission("negotiation:manage")
    public Result<NegotiationSession> publish(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.publish(sessionId));
    }

    /**
     * 开始谈判 / Start negotiation
     */
    @PostMapping("/session/{sessionId}/start")
    @RequirePermission("negotiation:manage")
    public Result<NegotiationSession> startNegotiation(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.startNegotiation(sessionId));
    }

    /**
     * 提交一轮报价 / Submit round bid
     */
    @PostMapping("/round")
    @RequirePermission("negotiation:add")
    public Result<NegotiationRound> submitOffer(@RequestBody NegotiationRound round) {
        return Result.ok(negotiationService.submitOffer(round));
    }

    /**
     * 最低价评审判定标 / Lowest price evaluation and award
     */
    @PostMapping("/session/{sessionId}/award")
    @RequirePermission("negotiation:manage")
    public Result<NegotiationSession> award(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.award(sessionId));
    }

    /**
     * 终止谈判 / Terminate negotiation
     */
    @PostMapping("/session/{sessionId}/terminate")
    @RequirePermission("negotiation:manage")
    public Result<NegotiationSession> terminate(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.terminate(sessionId));
    }

    /**
     * 分页查询谈判会话（支持 keyword + status 筛选） / Paginated query of negotiation sessions (supports keyword + status filtering)
     */
    @GetMapping("/session/page")
    @RequirePermission("negotiation:view")
    public Result<PageResult<NegotiationSession>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NegotiationSession> result = negotiationService.page(keyword, status, page, size);
        return PageResult.ok(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 查询谈判会话详情 / Query negotiation session detail
     */
    @GetMapping("/session/{sessionId}")
    @RequirePermission("negotiation:view")
    public Result<NegotiationSession> getSession(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.getSession(sessionId));
    }

    /**
     * 查询谈判报价记录列表 / Query negotiation bid record list
     */
    @GetMapping("/session/{sessionId}/rounds")
    @RequirePermission("negotiation:view")
    public Result<List<NegotiationRound>> listRounds(@PathVariable Long sessionId) {
        return Result.ok(negotiationService.listRounds(sessionId));
    }
}
