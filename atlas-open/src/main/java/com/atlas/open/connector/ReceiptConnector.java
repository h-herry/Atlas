package com.atlas.open.connector;

import com.atlas.open.service.DynamicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 收货数据同步连接器 — 将收货/入库数据同步到外部 WMS/ERP / Receipt data sync connector — syncs receipt/inbound data to external WMS/ERP
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Component
@RequiredArgsConstructor
public class ReceiptConnector {

    private final DynamicApiService dynamicApiService;

    /**
     * 同步收货单到外部系统 / Sync receipt order to external system
     */
    public Object syncReceipt(String endpointCode, String receiptNo, String orderNo,
                               Long warehouseId, BigDecimal totalQty, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("receiptNo", receiptNo);
        params.put("orderNo", orderNo);
        params.put("warehouseId", String.valueOf(warehouseId));
        params.put("totalQty", totalQty.toPlainString());
        params.put("status", status);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 同步质检结果 / Sync quality inspection result
     */
    public Object syncQualityCheck(String endpointCode, String receiptNo,
                                    String checkResult, String inspector) {
        Map<String, Object> params = new HashMap<>();
        params.put("receiptNo", receiptNo);
        params.put("checkResult", checkResult);
        params.put("inspector", inspector);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 确认入库同步 / Confirm inbound sync
     */
    public Object confirmRestock(String endpointCode, String receiptNo, Long warehouseId) {
        Map<String, Object> params = new HashMap<>();
        params.put("receiptNo", receiptNo);
        params.put("warehouseId", String.valueOf(warehouseId));
        return dynamicApiService.execute(endpointCode, params);
    }
}
