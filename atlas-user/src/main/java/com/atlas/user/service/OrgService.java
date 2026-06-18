package com.atlas.user.service;

import com.atlas.user.entity.OrgNode;
import com.atlas.user.mapper.OrgNodeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织架构 Service — 树形查询 / 按类型筛选 /
 * Organization structure Service — tree query / type filter
 *
 * <p>支持 集团→事业部→工厂→车间→产线 5 级层级，供应商供货范围可关联到产线级 /
 * Supports Group → BU → Plant → Workshop → Line 5-level hierarchy; supplier supply scope can link to line level</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgNodeMapper orgNodeMapper;

    /**
     * 查询完整组织树（逐级构建）/ Query full org tree (build hierarchically)
     */
    public List<OrgNode> getFullTree() {
        List<OrgNode> allNodes = orgNodeMapper.findFullTree();
        return buildTree(allNodes, null);
    }

    /**
     * 按类型筛选组织节点 / Filter org nodes by type
     */
    public List<OrgNode> listByType(String nodeType) {
        return orgNodeMapper.findByNodeType(nodeType);
    }

    /**
     * 按父节点查子节点 / Query children by parent
     */
    public List<OrgNode> getChildren(Long parentId) {
        return orgNodeMapper.findByParentId(parentId);
    }

    /**
     * 创建组织节点 / Create org node
     */
    @Transactional(rollbackFor = Exception.class)
    public OrgNode create(OrgNode node) {
        // 自动生成 nodePath / Auto-generate node path
        if (node.getParentId() != null && node.getParentId() > 0) {
            OrgNode parent = orgNodeMapper.selectById(node.getParentId());
            if (parent != null) {
                node.setNodePath(parent.getNodePath() + node.getParentId() + "/");
                node.setNodeLevel(parent.getNodeLevel() + 1);
            }
        } else {
            node.setNodePath("/");
            node.setNodeLevel(1);
        }
        node.setStatus(1);
        orgNodeMapper.insert(node);
        log.info("组织节点已创建: name={}, type={}", node.getName(), node.getNodeType());
        return node;
    }

    /**
     * 按ID查节点 / Query node by ID
     */
    public OrgNode getById(Long nodeId) {
        return orgNodeMapper.selectById(nodeId);
    }

    /**
     * 更新节点 / Update node
     */
    @Transactional(rollbackFor = Exception.class)
    public OrgNode update(Long nodeId, OrgNode update) {
        OrgNode existing = orgNodeMapper.selectById(nodeId);
        if (existing == null) {
            throw new IllegalArgumentException("组织节点不存在 / Org node not found: " + nodeId);
        }
        existing.setName(update.getName());
        existing.setNodeType(update.getNodeType());
        existing.setManagerId(update.getManagerId());
        existing.setSortOrder(update.getSortOrder());
        existing.setStatus(update.getStatus());
        orgNodeMapper.updateById(existing);
        return existing;
    }

    /**
     * 递归构建树 / Recursively build tree
     */
    private List<OrgNode> buildTree(List<OrgNode> allNodes, Long parentId) {
        List<OrgNode> tree = new ArrayList<>();
        for (OrgNode node : allNodes) {
            if ((parentId == null && node.getParentId() == null) ||
                (parentId != null && parentId.equals(node.getParentId()))) {
                List<OrgNode> children = buildTree(allNodes, node.getNodeId());
                tree.add(node);
                tree.addAll(children);
            }
        }
        return tree;
    }
}
