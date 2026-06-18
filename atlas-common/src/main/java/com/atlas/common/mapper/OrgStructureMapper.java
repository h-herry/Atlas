package com.atlas.common.mapper;

import com.atlas.common.entity.OrgStructure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织架构 Mapper / Organization Structure Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface OrgStructureMapper extends BaseMapper<OrgStructure> {

    /**
     * 查询节点的所有子孙节点（基于 node_path 前缀匹配） /
     * Query all descendant nodes (based on node_path prefix matching)
     *
     * @param nodePath 父节点路径 / Parent node path
     * @return 子孙节点列表 / List of descendant nodes
     */
    @Select("SELECT * FROM org_structure WHERE node_path LIKE CONCAT(#{nodePath}, '%') AND status = 1")
    List<OrgStructure> selectDescendants(@Param("nodePath") String nodePath);

    /**
     * 查询指定层级的所有节点 / Query all nodes at a given level
     *
     * @param nodeType 节点类型 / Node type (GROUP/DIVISION/PLANT/WORKSHOP/LINE)
     * @return 节点列表 / Node list
     */
    @Select("SELECT * FROM org_structure WHERE node_type = #{nodeType} AND status = 1")
    List<OrgStructure> selectByNodeType(@Param("nodeType") String nodeType);

    /**
     * 查询节点的直接子节点 / Query direct children of a node
     *
     * @param parentId 父节点ID / Parent node ID
     * @return 子节点列表 / Child node list
     */
    @Select("SELECT * FROM org_structure WHERE parent_id = #{parentId} AND status = 1")
    List<OrgStructure> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 按节点路径查询单个节点 / Query single node by path
     *
     * @param nodePath 节点路径 / Node path
     * @return 组织节点 / Org node
     */
    @Select("SELECT * FROM org_structure WHERE node_path = #{nodePath} AND status = 1")
    OrgStructure selectByNodePath(@Param("nodePath") String nodePath);
}
