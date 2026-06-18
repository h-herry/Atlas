package com.atlas.purchase.mapper;

import com.atlas.common.security.annotation.DataScope;
import com.atlas.purchase.entity.InquiryPurchase;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 询比采购 Mapper — 数据权限隔离：按部门过滤 /
 * Inquiry purchase Mapper — data scope isolation: filter by department
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface InquiryPurchaseMapper extends BaseMapper<InquiryPurchase> {

    /**
     * 分页查询（带数据权限） / Paginated query with data scope
     */
    @DataScope(deptColumn = "dept_id")
    IPage<InquiryPurchase> selectPageWithScope(IPage<InquiryPurchase> page, @Param(Constants.WRAPPER) Wrapper<InquiryPurchase> wrapper);

    /**
     * 列表查询（带数据权限） / List query with data scope
     */
    @DataScope(deptColumn = "dept_id")
    List<InquiryPurchase> selectListWithScope(@Param(Constants.WRAPPER) Wrapper<InquiryPurchase> wrapper);
}
