package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.dto.InviteRequest;
import com.atlas.purchase.entity.InvitedBidding;
import com.atlas.purchase.entity.InvitedBiddingSupplier;
import com.atlas.purchase.service.InvitedBiddingService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邀请招标 REST API / Invited bidding REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/invited-bidding")
@RequiredArgsConstructor
public class InvitedBiddingController {

    private final InvitedBiddingService invitedBiddingService;

    /**
     * 分页查询邀请招标 / Paginated query of invited biddings
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<InvitedBidding>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<InvitedBidding> page = invitedBiddingService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询邀请招标详情 / Query invited bidding detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<InvitedBidding> getById(@PathVariable Long id) {
        return Result.success(invitedBiddingService.getById(id));
    }

    /**
     * 查询邀请供应商列表 / Query invited supplier list
     */
    @GetMapping("/{id}/suppliers")
    @RequirePermission("purchase:bidding:view")
    public Result<List<InvitedBiddingSupplier>> listSuppliers(@PathVariable Long id) {
        return Result.success(invitedBiddingService.listSuppliers(id));
    }

    /**
     * 发送邀请 / Send invitations
     */
    @PostMapping("/{id}/invite")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> inviteSuppliers(@PathVariable Long id,
                                         @RequestBody InviteRequest request) {
        invitedBiddingService.inviteSuppliers(id, request.getSupplierIds(), request.getSupplierNames(),
            request.getInvitationReason(),
            request.getBidEndDate() != null ? java.time.LocalDate.parse(request.getBidEndDate()) : null,
            request.getBidOpeningDate() != null ? java.time.LocalDate.parse(request.getBidOpeningDate()) : null);
        return Result.success();
    }

    /**
     * 接受邀请 / Accept invitation
     */
    @PutMapping("/supplier/{supplierRecordId}/accept")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> acceptInvite(@PathVariable Long supplierRecordId) {
        invitedBiddingService.acceptInvite(supplierRecordId);
        return Result.success();
    }

    /**
     * 拒绝邀请 / Reject invitation
     */
    @PutMapping("/supplier/{supplierRecordId}/reject")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> rejectInvite(@PathVariable Long supplierRecordId) {
        invitedBiddingService.rejectInvite(supplierRecordId);
        return Result.success();
    }

    /**
     * 开始投标 / Start bidding
     */
    @PutMapping("/{id}/start-bidding")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startBidding(@PathVariable Long id) {
        invitedBiddingService.startBidding(id);
        return Result.success();
    }

    /**
     * 提交投标 / Submit bid
     */
    @PutMapping("/supplier/{supplierRecordId}/submit-bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> submitBid(@PathVariable Long supplierRecordId,
                                   @RequestParam java.math.BigDecimal bidAmount,
                                   @RequestParam(required = false) String bidFileUrl) {
        invitedBiddingService.submitBid(supplierRecordId, bidAmount, bidFileUrl);
        return Result.success();
    }

    /**
     * 开标 / Open bid
     */
    @PutMapping("/{id}/open-bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> openBid(@PathVariable Long id) {
        invitedBiddingService.openBid(id);
        return Result.success();
    }

    /**
     * 供应商评分 / Score supplier
     */
    @PutMapping("/supplier/{supplierRecordId}/evaluate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> evaluate(@PathVariable Long supplierRecordId,
                                  @RequestParam java.math.BigDecimal score) {
        invitedBiddingService.evaluate(supplierRecordId, score);
        return Result.success();
    }

    /**
     * 开始评标 / Start evaluation
     */
    @PutMapping("/{id}/start-evaluation")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startEvaluation(@PathVariable Long id) {
        invitedBiddingService.startEvaluation(id);
        return Result.success();
    }

    /**
     * 定标 / Award
     */
    @PutMapping("/{id}/award")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> award(@PathVariable Long id) {
        invitedBiddingService.award(id);
        return Result.success();
    }

    /**
     * 终止招标 / Terminate bidding
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        invitedBiddingService.terminate(id);
        return Result.success();
    }
}
