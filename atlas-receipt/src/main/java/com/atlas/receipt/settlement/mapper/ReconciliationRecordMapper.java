package com.atlas.receipt.settlement.mapper;

import com.atlas.receipt.settlement.entity.ReconciliationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对账记录 Mapper / Reconciliation record Mapper
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Mapper
public interface ReconciliationRecordMapper extends BaseMapper<ReconciliationRecord> {
}
