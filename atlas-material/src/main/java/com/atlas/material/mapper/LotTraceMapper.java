package com.atlas.material.mapper;

import com.atlas.material.entity.LotTrace;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 来料批次追溯 Mapper / Incoming lot trace Mapper
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Mapper
public interface LotTraceMapper extends BaseMapper<LotTrace> {

    /**
     * 按批次号模糊搜索 / Fuzzy search by lot number
     */
    List<LotTrace> selectByLotNo(@Param("lotNo") String lotNo);

    /**
     * 按物料 ID 查所有关联批次 / Find all lots by material ID
     */
    List<LotTrace> selectByMaterialId(@Param("materialId") Long materialId);

    /**
     * 按状态筛选 / Filter by status
     */
    List<LotTrace> selectByStatus(@Param("status") String status);
}
