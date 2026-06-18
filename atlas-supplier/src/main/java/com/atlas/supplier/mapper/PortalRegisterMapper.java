package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.PortalRegister;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商入驻申请 Mapper — portal 端 / Supplier onboarding application Mapper — portal side
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Mapper
public interface PortalRegisterMapper extends BaseMapper<PortalRegister> {
}
