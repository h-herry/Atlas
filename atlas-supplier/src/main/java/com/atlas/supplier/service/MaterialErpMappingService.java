package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MaterialErpMapping;
import com.atlas.supplier.mapper.MaterialErpMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ERP 物料编码映射 Service — 映射配置 / 同步状态查询 / 解绑 /
 * ERP material code mapping Service — mapping configuration / sync status query / unbind
 *
 * <p>支持 SAP / 金蝶(KINGDEE) / 用友(U8) 等多 ERP 系统，
 * 一个 Atlas 物料可映射多个 ERP 系统的编码，按工厂维度隔离。 /
 * Supports SAP / Kingdee / U8 and other ERP systems,
 * one Atlas material can map to multiple ERP system codes, isolated by plant dimension.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialErpMappingService {

    private final MaterialErpMappingMapper materialErpMappingMapper;

    // ==================== 映射配置 / Mapping Configuration ====================

    /**
     * 创建 ERP 编码映射 / Create ERP code mapping
     *
     * @param mapping 映射对象 / Mapping object
     * @return 创建后的映射 / Created mapping
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialErpMapping create(MaterialErpMapping mapping) {
        // 检查重复 / Check duplicate
        List<MaterialErpMapping> existing = materialErpMappingMapper.selectList(
            new LambdaQueryWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getMaterialId, mapping.getMaterialId())
                .eq(MaterialErpMapping::getErpSystem, mapping.getErpSystem())
                .eq(MaterialErpMapping::getStatus, 1));
        if (!existing.isEmpty()) {
            throw new BizException(ErrorCode.DATA_EXIST,
                "该物料在 " + mapping.getErpSystem() + " 系统中已存在映射");
        }
        mapping.setMappedTime(LocalDateTime.now());
        mapping.setStatus(1);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());
        materialErpMappingMapper.insert(mapping);
        log.info("创建 ERP 映射: materialId={}, erpSystem={}, erpCode={}",
            mapping.getMaterialId(), mapping.getErpSystem(), mapping.getErpMaterialCode());
        return mapping;
    }

    /**
     * 按 Atlas 物料ID 查询所有 ERP 映射 / Query all ERP mappings by Atlas material ID
     *
     * @param materialId Atlas 物料ID / Atlas material ID
     * @return 有效映射列表 / Active mapping list
     */
    public List<MaterialErpMapping> findByMaterialId(Long materialId) {
        return materialErpMappingMapper.selectList(
            new LambdaQueryWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getMaterialId, materialId)
                .eq(MaterialErpMapping::getStatus, 1));
    }

    /**
     * 按 ERP 系统 + 编码反查 Atlas 物料ID / Reverse-lookup Atlas material ID by ERP system + code
     *
     * @param erpSystem       ERP 系统 / ERP system
     * @param erpMaterialCode ERP 物料编码 / ERP material code
     * @return 映射记录 / Mapping record
     */
    public MaterialErpMapping findByErpCode(String erpSystem, String erpMaterialCode) {
        return materialErpMappingMapper.selectOne(
            new LambdaQueryWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getErpSystem, erpSystem)
                .eq(MaterialErpMapping::getErpMaterialCode, erpMaterialCode)
                .eq(MaterialErpMapping::getStatus, 1));
    }

    // ==================== 同步状态查询 / Sync Status Query ====================

    /**
     * 查询指定 ERP 系统的所有有效映射 / Query all active mappings for a specific ERP system
     *
     * @param erpSystem ERP 系统 / ERP system
     * @return 映射列表 / Mapping list
     */
    public List<MaterialErpMapping> listByErpSystem(String erpSystem) {
        return materialErpMappingMapper.selectList(
            new LambdaQueryWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getErpSystem, erpSystem)
                .eq(MaterialErpMapping::getStatus, 1));
    }

    /**
     * 查询最近映射记录 / Query recent mappings
     *
     * @param limit 条数限制 / Size limit
     * @return 最近映射列表 / Recent mapping list
     */
    public List<MaterialErpMapping> listRecent(int limit) {
        return materialErpMappingMapper.selectList(
            new LambdaQueryWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getStatus, 1)
                .orderByDesc(MaterialErpMapping::getMappedTime)
                .last("LIMIT " + Math.min(limit, 100)));
    }

    // ==================== 解绑 / Unbind ====================

    /**
     * 解绑映射（软删除） / Unbind mapping (soft delete)
     *
     * @param id 映射ID / Mapping ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long id) {
        materialErpMappingMapper.update(null,
            new LambdaUpdateWrapper<MaterialErpMapping>()
                .eq(MaterialErpMapping::getId, id)
                .set(MaterialErpMapping::getStatus, 0)
                .set(MaterialErpMapping::getUpdatedAt, LocalDateTime.now()));
        log.info("解绑 ERP 映射: id={}", id);
    }

    /**
     * 批量解绑 / Batch unbind
     *
     * @param ids 映射ID列表 / Mapping ID list
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindBatch(List<Long> ids) {
        for (Long id : ids) {
            unbind(id);
        }
    }
}
