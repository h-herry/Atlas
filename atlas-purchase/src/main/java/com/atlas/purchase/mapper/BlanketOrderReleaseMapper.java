package com.atlas.purchase.mapper;

import com.atlas.purchase.entity.BlanketOrderRelease;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 一揽子订单分批释放 Mapper / Blanket order release Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface BlanketOrderReleaseMapper extends BaseMapper<BlanketOrderRelease> {
}
