package com.atlas.purchase.settlement.mapper;

import com.atlas.purchase.settlement.entity.AgingAnalysis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 账龄分析 Mapper / Aging analysis Mapper
 *
 * @since 1.2.22
 */
@Mapper
public interface AgingAnalysisMapper extends BaseMapper<AgingAnalysis> {

    /**
     * 查指定截止日期的账龄记录 / Query aging records by as-of date
     */
    @Select("SELECT * FROM aging_analysis WHERE as_of_date = #{asOfDate} ORDER BY total_payable DESC")
    List<AgingAnalysis> findByAsOfDate(@Param("asOfDate") LocalDate asOfDate);

    /**
     * 查询超90天预警的供应商 / Query suppliers with 90+ day overdue flag
     */
    @Select("SELECT * FROM aging_analysis WHERE overdue_flag = 1 AND as_of_date = #{asOfDate}")
    List<AgingAnalysis> findOverdue(@Param("asOfDate") LocalDate asOfDate);
}
