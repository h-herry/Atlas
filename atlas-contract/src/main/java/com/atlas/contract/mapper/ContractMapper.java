package com.atlas.contract.mapper;

import com.atlas.contract.entity.Contract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {
}