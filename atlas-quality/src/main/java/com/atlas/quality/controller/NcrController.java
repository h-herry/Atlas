package com.atlas.quality.controller;

import com.atlas.common.core.result.Result;
import com.atlas.quality.entity.NcrRecord;
import com.atlas.quality.service.NcrService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 不合格品处理（NCR）控制器 / Non-Conformance Report (NCR) controller
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/quality/ncr")
@RequiredArgsConstructor
public class NcrController {

    private final NcrService ncrService;

    /**
     * 创建 NCR / Create NCR
     */
    @PostMapping
    public Result<NcrRecord> createNcr(@RequestBody CreateNcrRequest request) {
        NcrRecord ncr = ncrService.createNcr(
                request.getInspectId(),
                request.getMaterialId(),
                request.getMaterialName(),
                request.getBatchNo(),
                request.getSupplierId(),
                request.getDefectType(),
                request.getDefectDescription(),
                request.getDefectQty(),
                request.getDefectSeverity(),
                request.getCreatedBy());
        return Result.success(ncr);
    }

    /**
     * 处置决策 / Disposition decision
     */
    @PutMapping("/{id}/dispose")
    public Result<Void> dispose(@PathVariable Long id, @RequestBody DisposeRequest request) {
        ncrService.dispose(id, request.getDisposition(), request.getDispositionBy(),
                request.getDispositionReason(), request.getCorrectiveAction());
        return Result.success();
    }

    /**
     * 闭环 NCR / Close NCR
     */
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        ncrService.close(id);
        return Result.success();
    }

    /**
     * 按 ID 查询 / Query by ID
     */
    @GetMapping("/{id}")
    public Result<NcrRecord> getById(@PathVariable Long id) {
        return Result.success(ncrService.getById(id));
    }

    /**
     * 分页查询 / Paginated query
     */
    @GetMapping
    public Result<Page<NcrRecord>> page(
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String disposition,
            @RequestParam(required = false) Integer closed,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(ncrService.page(materialId, supplierId, disposition, closed, page, size));
    }

    /**
     * 查询未闭环 NCR / Query open NCRs
     */
    @GetMapping("/open")
    public Result<List<NcrRecord>> listOpen() {
        return Result.success(ncrService.listOpen());
    }

    // ==================== 请求体 / Request Body ====================

    @lombok.Data
    public static class CreateNcrRequest {
        private Long inspectId;
        private Long materialId;
        private String materialName;
        private String batchNo;
        private Long supplierId;
        private String defectType;
        private String defectDescription;
        private java.math.BigDecimal defectQty;
        private String defectSeverity;
        private Long createdBy;
    }

    @lombok.Data
    public static class DisposeRequest {
        private String disposition;
        private Long dispositionBy;
        private String dispositionReason;
        private String correctiveAction;
    }
}
