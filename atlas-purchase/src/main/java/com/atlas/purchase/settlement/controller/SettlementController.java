package com.atlas.purchase.settlement.controller;

import com.atlas.purchase.settlement.entity.AgingAnalysis;
import com.atlas.purchase.settlement.entity.SettlementRecon;
import com.atlas.purchase.settlement.entity.SettlementReconDetail;
import com.atlas.purchase.settlement.entity.ThreeWayMatch;
import com.atlas.purchase.settlement.service.AgingAnalysisService;
import com.atlas.purchase.settlement.service.SettlementReconService;
import com.atlas.purchase.settlement.service.ThreeWayMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 结算管理 Controller — 三单匹配 / 对账单 / 账龄分析 /
 * Settlement management Controller — three-way match / reconciliation / aging analysis
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final ThreeWayMatchService threeWayMatchService;
    private final SettlementReconService settlementReconService;
    private final AgingAnalysisService agingAnalysisService;

    // ============================================================
    // 三单匹配 / Three-way match (P0-3.7.1)
    // ============================================================

    /**
     * 执行三单匹配 / Execute three-way match
     */
    @PostMapping("/three-way-match")
    public ThreeWayMatch executeMatch(@RequestBody Map<String, Object> body) {
        return threeWayMatchService.execute(
                Long.valueOf(body.get("poId").toString()),
                Long.valueOf(body.get("receiveId").toString()),
                Long.valueOf(body.get("invoiceId").toString()),
                new BigDecimal(body.get("poQty").toString()),
                new BigDecimal(body.get("receiveQty").toString()),
                new BigDecimal(body.get("invoiceQty").toString()),
                new BigDecimal(body.get("poUnitPrice").toString()),
                new BigDecimal(body.get("invoiceUnitPrice").toString()),
                body.get("materialId") != null ? Long.valueOf(body.get("materialId").toString()) : null,
                body.get("materialCode") != null ? body.get("materialCode").toString() : null
        );
    }

    /**
     * 按PO查询匹配记录 / Query match records by PO
     */
    @GetMapping("/three-way-match/po/{poId}")
    public List<ThreeWayMatch> listMatches(@PathVariable Long poId) {
        return threeWayMatchService.findByPoId(poId);
    }

    /**
     * 人工确认差异 / Manually confirm discrepancy
     */
    @PutMapping("/three-way-match/{matchId}/confirm")
    public ThreeWayMatch confirmMatch(@PathVariable Long matchId,
                                       @RequestBody Map<String, String> body) {
        return threeWayMatchService.confirm(matchId, body.get("resolution"),
                body.get("resolvedBy") != null ? Long.valueOf(body.get("resolvedBy")) : null,
                body.get("note"));
    }

    // ============================================================
    // 供应商对账单 / Settlement reconciliation (P0-3.7.4)
    // ============================================================

    /**
     * 生成对账单 / Generate reconciliation
     */
    @PostMapping("/recon")
    public SettlementRecon generate(@RequestBody Map<String, Object> body) {
        return settlementReconService.generate(
                Long.valueOf(body.get("supplierId").toString()),
                body.get("supplierName").toString(),
                LocalDate.parse(body.get("periodStart").toString()),
                LocalDate.parse(body.get("periodEnd").toString()),
                new BigDecimal(body.get("reconAmount").toString()),
                body.get("createdBy") != null ? Long.valueOf(body.get("createdBy").toString()) : null
        );
    }

    /**
     * 发送给供应商 / Send to supplier
     */
    @PutMapping("/recon/{reconId}/send")
    public SettlementRecon sendRecon(@PathVariable Long reconId) {
        return settlementReconService.send(reconId);
    }

    /**
     * 供应商确认 / Supplier confirms
     */
    @PutMapping("/recon/{reconId}/confirm")
    public SettlementRecon confirmRecon(@PathVariable Long reconId,
                                         @RequestBody Map<String, Object> body) {
        return settlementReconService.confirm(reconId,
                new BigDecimal(body.get("confirmedAmount").toString()));
    }

    /**
     * 供应商提交异议 / Supplier submits dispute
     */
    @PostMapping("/recon/{reconId}/dispute")
    public void dispute(@PathVariable Long reconId,
                        @RequestBody List<SettlementReconDetail> details) {
        settlementReconService.submitDispute(reconId, details);
    }

    /**
     * 按供应商查对账单 / List recons by supplier
     */
    @GetMapping("/recon/supplier/{supplierId}")
    public List<SettlementRecon> listBySupplier(@PathVariable Long supplierId) {
        return settlementReconService.listBySupplier(supplierId);
    }

    /**
     * 查对账单差异明细 / Query recon discrepancy details
     */
    @GetMapping("/recon/{reconId}/details")
    public List<SettlementReconDetail> listDetails(@PathVariable Long reconId) {
        return settlementReconService.listDetails(reconId);
    }

    // ============================================================
    // 账龄分析 / Aging analysis (P1-3.7.5)
    // ============================================================

    /**
     * 查询账龄分析 GET /api/settlement/aging?asOf=YYYY-MM-DD /
     * Query aging analysis by as-of date
     */
    @GetMapping("/aging")
    public List<AgingAnalysis> queryAging(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return agingAnalysisService.queryByDate(asOf);
    }

    /**
     * 查询超90天预警 / Query overdue (90+ days) suppliers
     */
    @GetMapping("/aging/overdue")
    public List<AgingAnalysis> listOverdue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return agingAnalysisService.findOverdue(asOf);
    }
}
