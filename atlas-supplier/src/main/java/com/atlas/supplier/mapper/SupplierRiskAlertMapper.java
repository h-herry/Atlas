package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierRiskAlert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商风险预警 Mapper — 对应 supplier_risk_alert 表 /
 * Supplier risk alert Mapper — maps to supplier_risk_alert table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface SupplierRiskAlertMapper extends BaseMapper<SupplierRiskAlert> {
}
