package com.atlas.user.controller;

import com.atlas.user.entity.OrgNode;
import com.atlas.user.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 组织架构 Controller — 树形查询 / 按类型筛选 /
 * Organization structure Controller — tree query / type filter
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/system/org")
@RequiredArgsConstructor
@Tag(name = "组织管理 / Organization Management")
public class OrgController {

    private final OrgService orgService;

    /**
     * 查询完整组织树 / Get full organization tree
     */
    @GetMapping("/tree")
    public List<OrgNode> getTree() {
        return orgService.getFullTree();
    }

    /**
     * 按类型筛选 / Filter by node type
     */
    @GetMapping("/type/{nodeType}")
    public List<OrgNode> listByType(@PathVariable String nodeType) {
        return orgService.listByType(nodeType);
    }

    /**
     * 查子节点 / Get children by parent
     */
    @GetMapping("/children/{parentId}")
    public List<OrgNode> getChildren(@PathVariable Long parentId) {
        return orgService.getChildren(parentId);
    }

    /**
     * 创建节点 / Create node
     */
    @PostMapping
    public OrgNode create(@RequestBody OrgNode node) {
        return orgService.create(node);
    }

    /**
     * 按ID查节点 / Get node by ID
     */
    @GetMapping("/{nodeId}")
    public OrgNode getById(@PathVariable Long nodeId) {
        return orgService.getById(nodeId);
    }

    /**
     * 更新节点 / Update node
     */
    @PutMapping("/{nodeId}")
    public OrgNode update(@PathVariable Long nodeId, @RequestBody OrgNode node) {
        return orgService.update(nodeId, node);
    }
}
