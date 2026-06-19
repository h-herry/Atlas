package com.atlas.material.mapper;

import com.atlas.material.entity.Material;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料主数据 Mapper / Material master data Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    /**
     * 按物料编码精确查询 / Query by material code (exact match)
     */
    Material selectByMaterialCode(@Param("materialCode") String materialCode);

    /**
     * 按物料名称模糊搜索 / Fuzzy search by material name
     */
    List<Material> selectByMaterialName(@Param("keyword") String keyword);
}
