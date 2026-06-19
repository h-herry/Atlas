package com.atlas.receipt.settlement.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.receipt.settlement.entity.PaymentRecord;
import com.atlas.receipt.settlement.service.PaymentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 付款管理 REST API / Payment management REST API
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "付款管理 / Payment Management")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建付款申请 / Create payment application
     */
    @PostMapping
    @RequirePermission("payment:create")
    public Result<PaymentRecord> create(@RequestParam @NotNull Long settlementId,
                                        @RequestParam @NotNull BigDecimal payAmount,
                                        @RequestParam @NotBlank String payMethod,
                                        @RequestParam @NotNull Long createdBy,
                                        @RequestParam(required = false) String remark) {
        return Result.ok(paymentService.create(settlementId, payAmount, payMethod, createdBy, remark));
    }

    /**
     * 审批付款 / Approve payment
     */
    @PutMapping("/{id}/approve")
    @RequirePermission("payment:approve")
    public Result<PaymentRecord> approve(@PathVariable Long id,
                                          @RequestParam @NotNull Long approvedBy) {
        return Result.ok(paymentService.approve(id, approvedBy));
    }

    /**
     * 确认支付 / Confirm payment
     */
    @PutMapping("/{id}/pay")
    @RequirePermission("payment:pay")
    public Result<PaymentRecord> pay(@PathVariable Long id,
                                      @RequestParam @NotNull Long paidBy) {
        return Result.ok(paymentService.pay(id, paidBy));
    }

    /**
     * 分页查询付款记录 / Paginated query of payment records
     */
    @GetMapping("/list")
    @RequirePermission("payment:view")
    public Result<PageResult<PaymentRecord>> list(
            @RequestParam(required = false) Long settlementId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PaymentRecord> pageResult = paymentService.list(settlementId, status, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }
}
