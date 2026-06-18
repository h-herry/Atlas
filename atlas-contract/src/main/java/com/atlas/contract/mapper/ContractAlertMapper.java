package com.atlas.contract.mapper;

import com.atlas.contract.entity.ContractAlert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 合同到期提醒 Mapper / Contract alert Mapper
 *
 * @since 1.2.22
 */
@Mapper
public interface ContractAlertMapper extends BaseMapper<ContractAlert> {

    /**
     * 查询指定提醒日期的待发送提醒 / Query pending alerts for a specific alert date
     */
    @Select("SELECT * FROM contract_alert WHERE alert_date = #{alertDate} AND status = 'PENDING'")
    List<ContractAlert> findPendingByDate(@Param("alertDate") LocalDate alertDate);
}
