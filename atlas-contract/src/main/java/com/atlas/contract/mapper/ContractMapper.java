package com.atlas.contract.mapper;

import com.atlas.common.security.annotation.DataScope;
import com.atlas.contract.entity.Contract;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 合同 Mapper — 数据权限隔离：按部门过滤 /
 * Contract Mapper — data scope isolation: filter by department
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /**
     * 分页查询（带数据权限） / Paginated query with data scope
     */
    @DataScope(deptColumn = "dept_id")
    IPage<Contract> selectPageWithScope(IPage<Contract> page, @Param(Constants.WRAPPER) Wrapper<Contract> wrapper);

    /**
     * 列表查询（带数据权限） / List query with data scope
     */
    @DataScope(deptColumn = "dept_id")
    List<Contract> selectListWithScope(@Param(Constants.WRAPPER) Wrapper<Contract> wrapper);
}