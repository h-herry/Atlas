package com.atlas.quality.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.quality.entity.InspectionStandard;
import com.atlas.quality.mapper.InspectionStandardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验标准与抽样方案服务 / Inspection standard & sampling plan service
 * <p>
 * 提供标准的 CRUD、按物料+检验类型查询、启用/停用。 /
 * Provides standard CRUD, query by material + inspection type, enable/disable.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionStandardService {

    private final InspectionStandardMapper standardMapper;

    /**
     * 创建检验标准 / Create inspection standard
     *
     * @param standard 检验标准 / Inspection standard
     * @return 创建后的标准 / Created standard
     */
    @Transactional(rollbackFor = Exception.class)
    public InspectionStandard create(InspectionStandard standard) {
        // 检查同一物料+检验类型是否已有启用标准 / Check for duplicate active standard
        Long count = standardMapper.selectCount(
                new LambdaQueryWrapper<InspectionStandard>()
                        .eq(InspectionStandard::getMaterialId, standard.getMaterialId())
                        .eq(InspectionStandard::getInspectType, standard.getInspectType())
                        .eq(InspectionStandard::getIsActive, 1));
        if (count > 0) {
            throw new BizException(6401, "该物料+检验类型的启用标准已存在，请先停用旧标准 / "
                    + "Active standard for this material + inspection type already exists; deactivate the old one first");
        }

        // 生成标准编号 / Generate standard number
        standard.setStandardNo("STD" + System.currentTimeMillis());
        standard.setIsActive(1);
        standardMapper.insert(standard);
        log.info("检验标准创建: standardNo={} materialId={} type={}",
                standard.getStandardNo(), standard.getMaterialId(), standard.getInspectType());
        return standard;
    }

    /**
     * 更新检验标准 / Update inspection standard
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(InspectionStandard standard) {
        InspectionStandard existing = standardMapper.selectById(standard.getId());
        if (existing == null) {
            throw new BizException(6402, "检验标准不存在 / Inspection standard not found");
        }
        standardMapper.updateById(standard);
        log.info("检验标准已更新: standardNo={}", standard.getStandardNo());
    }

    /**
     * 启用/停用标准 / Enable or disable standard
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long standardId, boolean active) {
        InspectionStandard standard = standardMapper.selectById(standardId);
        if (standard == null) {
            throw new BizException(6402, "检验标准不存在 / Inspection standard not found");
        }
        standard.setIsActive(active ? 1 : 0);
        standardMapper.updateById(standard);
        log.info("检验标准状态变更: standardNo={} active={}", standard.getStandardNo(), active);
    }

    /**
     * 按物料+检验类型查询标准 / Query standard by material + inspection type
     *
     * @param materialId  物料ID / Material ID
     * @param inspectType 检验类型 / Inspection type
     * @return 检验标准 / Inspection standard (nullable)
     */
    public InspectionStandard getByMaterialAndType(Long materialId, String inspectType) {
        return standardMapper.selectOne(
                new LambdaQueryWrapper<InspectionStandard>()
                        .eq(InspectionStandard::getMaterialId, materialId)
                        .eq(InspectionStandard::getInspectType, inspectType)
                        .eq(InspectionStandard::getIsActive, 1)
                        .last("LIMIT 1"));
    }

    /**
     * 按 ID 查询 / Query by ID
     */
    public InspectionStandard getById(Long id) {
        InspectionStandard standard = standardMapper.selectById(id);
        if (standard == null) {
            throw new BizException(6402, "检验标准不存在 / Inspection standard not found");
        }
        return standard;
    }

    /**
     * 分页查询检验标准 / Paginated query
     */
    public Page<InspectionStandard> page(Long materialId, String inspectType, int page, int size) {
        LambdaQueryWrapper<InspectionStandard> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(InspectionStandard::getMaterialId, materialId);
        }
        if (inspectType != null && !inspectType.isEmpty()) {
            wrapper.eq(InspectionStandard::getInspectType, inspectType);
        }
        wrapper.orderByDesc(InspectionStandard::getCreatedAt);
        return standardMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
