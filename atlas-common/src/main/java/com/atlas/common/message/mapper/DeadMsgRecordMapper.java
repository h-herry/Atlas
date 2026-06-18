package com.atlas.common.message.mapper;

import com.atlas.common.message.model.DeadMsgRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 死信消息记录 Mapper / Dead message record Mapper
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Mapper
public interface DeadMsgRecordMapper extends BaseMapper<DeadMsgRecord> {
}
