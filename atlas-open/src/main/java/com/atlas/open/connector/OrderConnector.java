package com.atlas.open.connector;

import com.atlas.open.service.DynamicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 采购订单同步连接器 — 将采购订单同步到外部 ERP/WMS /
 * Purchase order sync connector — syncs purchase orders to external ERP/WMS
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Component
@RequiredArgsConstructor
public class OrderConnector {

    private final DynamicApiService dynamicApiService;

    /**
     * 同步采购订单到外部系统 / Sync purchase order to external system
     */
    public Object syncOrder(String endpointCode, String orderNo, Long supplierId,
                             BigDecimal totalAmount, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("supplierId", String.valueOf(supplierId));
        params.put("totalAmount", totalAmount.toPlainString());
        params.put("status", status);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 同步订单状态变更 / Sync order status change
     */
    public Object syncOrderStatus(String endpointCode, String orderNo, String newStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("status", newStatus);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 查询外部订单状态 / Query external order status
     */
    public Object queryOrderStatus(String endpointCode, String orderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        return dynamicApiService.execute(endpointCode, params);
    }
}
