package com.atlas.receipt.controller;

import com.atlas.common.core.web.Result;
import com.atlas.receipt.entity.AsnItem;
import com.atlas.receipt.entity.AsnRecord;
import com.atlas.receipt.service.AsnService;
import com.atlas.receipt.service.AsnService.AsnItemRequest;
import com.atlas.common.security.annotation.RequirePermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * ASN 预先发货通知控制器 / ASN (Advanced Shipping Notice) controller
 * <p>
 * 供应商创建 ASN、采购确认收货、ASN 查询。 /
 * Supplier creates ASN, buyer confirms receipt, ASN query.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/asn")
@RequiredArgsConstructor
@Tag(name = "ASN管理 / ASN Management")
public class AsnController {

    private final AsnService asnService;

    /**
     * 供应商创建 ASN / Supplier creates ASN
     */
    @PostMapping
    @RequirePermission("delivery:asn:manage")
    public Result<AsnRecord> createAsn(@RequestBody CreateAsnRequest request) {
        AsnRecord asn = asnService.createAsn(
                request.getOrderId(),
                request.getSupplierId(),
                request.getExpectedArrivalDate(),
                request.getShipDate(),
                request.getCarrier(),
                request.getTrackingNo(),
                request.getItems(),
                request.getCreatedBy());
        return Result.success(asn);
    }

    /**
     * 按 ID 查询 ASN / Query ASN by ID
     */
    @GetMapping("/{id}")
    public Result<AsnRecord> getById(@PathVariable Long id) {
        return Result.success(asnService.getById(id));
    }

    /**
     * 查询 ASN 明细 / Query ASN items
     */
    @GetMapping("/{id}/items")
    public Result<List<AsnItem>> listItems(@PathVariable Long id) {
        return Result.success(asnService.listItems(id));
    }

    /**
     * 采购确认收货 / Buyer confirms receipt
     */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        asnService.confirmReceipt(id);
        return Result.success();
    }

    /**
     * 更新 ASN 状态 / Update ASN status
     */
    @PutMapping("/{id}/status")
    @RequirePermission("delivery:asn:manage")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        asnService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 分页查询 ASN / Paginated ASN query
     */
    @GetMapping
    @RequirePermission("delivery:asn:view")
    public Result<Page<AsnRecord>> page(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(asnService.page(orderId, supplierId, status, page, size));
    }

    // ==================== 请求体 / Request Body ====================

    @lombok.Data
    public static class CreateAsnRequest {
        private Long orderId;
        private Long supplierId;
        private LocalDate expectedArrivalDate;
        private LocalDate shipDate;
        private String carrier;
        private String trackingNo;
        private List<AsnItemRequest> items;
        private Long createdBy;
    }
}
