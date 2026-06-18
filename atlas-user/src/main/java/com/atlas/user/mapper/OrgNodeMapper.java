package com.atlas.user.mapper;

import com.atlas.user.entity.OrgNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织架构 Mapper / Organization structure Mapper
 *
 * @since 1.2.22
 */
@Mapper
public interface OrgNodeMapper extends BaseMapper<OrgNode> {

    /**
     * 按父节点查子节点 / Query children by parent
     */
    @Select("SELECT * FROM org_structure WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<OrgNode> findByParentId(@Param("parentId") Long parentId);

    /**
     * 按节点类型筛选 / Filter by node type
     */
    @Select("SELECT * FROM org_structure WHERE node_type = #{nodeType} ORDER BY sort_order")
    List<OrgNode> findByNodeType(@Param("nodeType") String nodeType);

    /**
     * 查询完整树（按路径排序）/ Query full tree (sorted by path)
     */
    @Select("SELECT * FROM org_structure WHERE status = 1 ORDER BY node_path, sort_order")
    List<OrgNode> findFullTree();
}
