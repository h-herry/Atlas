package com.atlas.quality.mapper;

import com.atlas.quality.entity.InspectionStandard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检验标准与抽样方案 Mapper / Inspection standard & sampling plan Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface InspectionStandardMapper extends BaseMapper<InspectionStandard> {
}
