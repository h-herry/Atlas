package com.atlas.purchase.inquiry.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.inquiry.entity.BiddingSession;
import com.atlas.purchase.inquiry.service.BiddingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 竞价大厅 REST API / Bidding hall REST API
 *
 * <p>Redis SortedSet 实时竞价排名。支持公开竞价(OPEN)和密封竞价(SEALED)。 /
 * Redis SortedSet real-time bidding ranking. Supports OPEN and SEALED bidding.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/bidding")
@RequiredArgsConstructor
@Tag(name = "竞价大厅 / Bidding Hall")
public class BiddingController {

    private final BiddingService biddingService;

    /**
     * 创建竞价场次 / Create bidding session
     */
    @PostMapping("/session")
    @RequirePermission("purchase:add")
    public Result<BiddingSession> createSession(@RequestBody BiddingSession session) {
        return Result.ok(biddingService.createSession(session));
    }

    /**
     * 启动竞价 / Start bidding session
     */
    @PostMapping("/session/{id}/start")
    @RequirePermission("purchase:edit")
    public Result<BiddingSession> startSession(@PathVariable Long id) {
        return Result.ok(biddingService.startSession(id));
    }

    /**
     * 供应商出价 / Supplier places bid
     */
    @PostMapping("/session/{id}/bid")
    @RequirePermission("purchase:edit")
    public Result<Integer> bid(@PathVariable Long id,
                                @RequestParam Long supplierId,
                                @RequestParam BigDecimal bidAmount) {
        return Result.ok(biddingService.bid(id, supplierId, bidAmount));
    }

    /**
     * 实时排名（公开竞价） / Real-time ranking (open bidding)
     */
    @GetMapping("/session/{id}/rank")
    @RequirePermission("purchase:view")
    public Result<List<Map<String, Object>>> rank(@PathVariable Long id) {
        return Result.ok(biddingService.getRanking(id));
    }

    /**
     * 关闭竞价 / Close bidding session
     */
    @PostMapping("/session/{id}/close")
    @RequirePermission("purchase:edit")
    public Result<Map<String, Object>> close(@PathVariable Long id) {
        return Result.ok(biddingService.closeSession(id));
    }
}
