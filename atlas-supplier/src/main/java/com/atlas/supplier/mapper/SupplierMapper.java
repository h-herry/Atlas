package com.atlas.supplier.mapper;

import com.atlas.common.security.annotation.DataScope;
import com.atlas.supplier.entity.Supplier;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商 Mapper — 数据权限隔离：按部门过滤 / Supplier Mapper — data scope isolation: filter by department
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * 分页查询（带数据权限） / Paginated query with data scope
     *
     * @param page    分页参数 / Page parameters
     * @param wrapper 查询条件 / Query conditions
     * @return 分页结果 / Paginated result
     */
    @DataScope(deptColumn = "dept_id")
    IPage<Supplier> selectPageWithScope(IPage<Supplier> page, @Param(Constants.WRAPPER) Wrapper<Supplier> wrapper);

    /**
     * 列表查询（带数据权限） / List query with data scope
     *
     * @param wrapper 查询条件 / Query conditions
     * @return 供应商列表 / Supplier list
     */
    @DataScope(deptColumn = "dept_id")
    List<Supplier> selectListWithScope(@Param(Constants.WRAPPER) Wrapper<Supplier> wrapper);
}