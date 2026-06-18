package com.atlas.open.connector;

import com.atlas.open.service.DynamicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 供应商数据同步连接器 — 将供应商信息同步到外部 ERP（SAP/Oracle/用友/金蝶） / Supplier data sync connector — syncs supplier info to external ERP (SAP/Oracle/Yonyou/Kingdee)
 * <p>
 * 封装标准的请求/响应格式，内部调用 DynamicApiService。 / Encapsulates standard request/response format, internally calls DynamicApiService.
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Component
@RequiredArgsConstructor
public class SupplierConnector {

    private final DynamicApiService dynamicApiService;

    /**
     * 同步供应商信息到外部系统 / Sync supplier info to external system
     *
     * @param endpointCode 接口编码（如 supplier-sync-sap） / @param endpointCode 接口编码（如 supplier-sync-sap） / Endpoint code (e.g. supplier-sync-sap)
     * @param supplierData 供应商数据 Map / @param supplierData 供应商数据 Map / Supplier data map
     * @return API 响应 / @return API 响应 / API response
     */
    public Object syncSupplier(String endpointCode, Map<String, Object> supplierData) {
        return dynamicApiService.execute(endpointCode, supplierData);
    }

    /**
     * 同步供应商新增 / Sync supplier creation
     */
    public Object syncSupplierCreate(String endpointCode, String supplierCode, String supplierName,
                                      String contact, String phone) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("supplierCode", supplierCode);
        params.put("supplierName", supplierName);
        params.put("contact", contact);
        params.put("phone", phone);
        return dynamicApiService.execute(endpointCode, params);
    }

    /**
     * 同步供应商资质更新 / Sync supplier qualification update
     */
    public Object syncSupplierQualification(String endpointCode, String supplierCode,
                                             String qualType, String qualLevel,
                                             String expireDate) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("supplierCode", supplierCode);
        params.put("qualType", qualType);
        params.put("qualLevel", qualLevel);
        params.put("expireDate", expireDate);
        return dynamicApiService.execute(endpointCode, params);
    }
}
