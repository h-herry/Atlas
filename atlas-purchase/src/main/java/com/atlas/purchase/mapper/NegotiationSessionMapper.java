package com.atlas.purchase.mapper;

import com.atlas.purchase.entity.NegotiationSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞争性谈判会话 Mapper / Competitive negotiation session Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface NegotiationSessionMapper extends BaseMapper<NegotiationSession> {
}
