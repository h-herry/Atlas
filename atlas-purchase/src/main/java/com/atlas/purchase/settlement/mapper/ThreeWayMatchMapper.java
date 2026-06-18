package com.atlas.purchase.settlement.mapper;

import com.atlas.purchase.settlement.entity.ThreeWayMatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 三单匹配 Mapper / Three-way match Mapper
 *
 * @since 1.2.22
 */
@Mapper
public interface ThreeWayMatchMapper extends BaseMapper<ThreeWayMatch> {

    /**
     * 按PO查询匹配记录 / Query match records by PO
     */
    @Select("SELECT * FROM three_way_match WHERE po_id = #{poId}")
    List<ThreeWayMatch> findByPoId(@Param("poId") Long poId);

    /**
     * 按状态统计匹配数 / Count by match status
     */
    @Select("SELECT match_status, COUNT(*) AS cnt FROM three_way_match WHERE po_id = #{poId} GROUP BY match_status")
    List<java.util.Map<String, Object>> countByStatus(@Param("poId") Long poId);
}
