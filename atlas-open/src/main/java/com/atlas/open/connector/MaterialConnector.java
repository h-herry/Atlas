package com.atlas.open.connector;

import com.atlas.open.service.DynamicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 物料主数据同步连接器 — 将物料/商品数据同步到外部 ERP/MDM /
 * Material master data sync connector — syncs material/product data to external ERP/MDM
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Component
@RequiredArgsConstructor
public class MaterialConnector {

    private final DynamicApiService dynamicApiService;

    /**
     * 同步物料主数据到外部系统 / Sync material master data to external system
     */
    public Object syncMaterial(String endpointCode, String materialCode, String materialName,
                                String categoryCode, String spec, String unit, String brand) {
        Map<String, Object> params = new HashMap<>();
        params.put("materialCode", materialCode);
        params.put("materialName", materialName);
        params.put("categoryCode", categoryCode);
        params.put("spec", spec);
        params.put("unit", unit);
        params.put("brand", brand);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 同步物料状态变更（启用/停用） / Sync material status change (enable/disable)
     */
    public Object syncMaterialStatus(String endpointCode, String materialCode, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("materialCode", materialCode);
        params.put("status", String.valueOf(status));
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 同步物料价格变更 / Sync material price change
     */
    public Object syncMaterialPrice(String endpointCode, String materialCode,
                                     String price, String effectiveDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("materialCode", materialCode);
        params.put("price", price);
        params.put("effectiveDate", effectiveDate);
        return dynamicApiService.execute(endpointCode, params);
    }
}
