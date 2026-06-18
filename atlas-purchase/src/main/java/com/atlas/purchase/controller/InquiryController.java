package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.dto.InquiryPublishRequest;
import com.atlas.purchase.entity.InquiryPurchase;
import com.atlas.purchase.entity.InquirySupplier;
import com.atlas.purchase.service.InquiryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 询比采购 Controller / Inquiry purchase Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 分页查询询比采购 / Paginated query of inquiry purchases
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<InquiryPurchase>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<InquiryPurchase> page = inquiryService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询询比采购详情 / Query inquiry purchase detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<InquiryPurchase> getById(@PathVariable Long id) {
        return Result.success(inquiryService.getById(id));
    }

    /**
     * 查询报价供应商列表 / Query quoting supplier list
     */
    @GetMapping("/{id}/suppliers")
    @RequirePermission("purchase:bidding:view")
    public Result<List<InquirySupplier>> listSuppliers(@PathVariable Long id) {
        return Result.success(inquiryService.listSuppliers(id));
    }

    /**
     * 发布询价 / Publish inquiry
     */
    @PostMapping("/{id}/publish")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> publish(@PathVariable Long id,
                                 @RequestBody InquiryPublishRequest request) {
        inquiryService.publish(id, request.getSupplierIds(), request.getSupplierNames(),
            request.getInquiryContent(),
            request.getDeadline() != null ? java.time.LocalDate.parse(request.getDeadline()) : null);
        return Result.success();
    }

    /**
     * 供应商报价 / Supplier submits quote
     */
    @PutMapping("/supplier/{inquirySupplierId}/quote")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> submitQuote(@PathVariable Long inquirySupplierId,
                                     @RequestParam java.math.BigDecimal quoteAmount,
                                     @RequestParam(required = false) Integer deliveryDays,
                                     @RequestParam(required = false) String paymentTerms,
                                     @RequestParam(required = false) String remark) {
        inquiryService.submitQuote(inquirySupplierId, quoteAmount, deliveryDays, paymentTerms, remark);
        return Result.success();
    }

    /**
     * 关闭报价 / Close quotation
     */
    @PutMapping("/{id}/close")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> closeQuotation(@PathVariable Long id) {
        inquiryService.closeQuotation(id);
        return Result.success();
    }

    /**
     * 进入比较 / Enter comparison
     */
    @PutMapping("/{id}/compare")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> compare(@PathVariable Long id) {
        inquiryService.compare(id);
        return Result.success();
    }

    /**
     * 定标 / Award
     */
    @PutMapping("/{id}/award")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> award(@PathVariable Long id) {
        inquiryService.award(id);
        return Result.success();
    }

    /**
     * 终止询比 / Terminate inquiry
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        inquiryService.terminate(id);
        return Result.success();
    }
}
