package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierBlacklist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商黑名单 Mapper / Supplier blacklist Mapper
 */
@Mapper
public interface SupplierBlacklistMapper extends BaseMapper<SupplierBlacklist> {

    /**
     * 查询供应商是否在黑名单中（生效状态） / Check if supplier is in blacklist (active status)
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 是否存在生效的黑名单记录 / Whether an active blacklist record exists
     */
    @Select("SELECT COUNT(*) > 0 FROM supplier_blacklist WHERE supplier_id = #{supplierId} AND status = 1")
    boolean existsActiveBySupplierId(@Param("supplierId") Long supplierId);

    /**
     * 按信用代码或企业名称精确匹配生效的黑名单记录 / Exact match by credit code or company name
     *
     * @param creditCode 统一社会信用代码 / Unified social credit code
     * @param name       企业名称 / Company name
     * @return 是否存在生效的黑名单记录 / Whether an active blacklist record exists
     */
    @Select("SELECT COUNT(*) > 0 FROM supplier_blacklist WHERE status = 1 AND (credit_code = #{creditCode} OR supplier_name = #{name})")
    boolean existsActiveByCreditCodeOrName(@Param("creditCode") String creditCode, @Param("name") String name);
}
