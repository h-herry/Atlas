package com.atlas.delivery.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.delivery.entity.VmiInventory;
import com.atlas.delivery.mapper.VmiInventoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VMI库存监控服务 — 水位监控、补货通知、库存预警 /
 * VMI inventory monitoring service — level monitoring, replenishment notification, stock alert
 * <p>
 * 核心能力： /
 * Core capabilities:
 * <ol>
 *   <li>定时检查库存低于安全库存 → 生成补货通知 / Scheduled check: stock below safety → generate replenishment notice</li>
 *   <li>定时检查库存高于最大库存 → 生成预警 / Scheduled check: stock above max → generate alert</li>
 *   <li>供应商门户端库存查询 / Supplier portal inventory query</li>
 *   <li>采购端库存看板（按供应商/物料/仓库维度） / Procurement dashboard (by supplier/material/warehouse)</li>
 * </ol>
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VmiInventoryService {

    private final VmiInventoryMapper vmiInventoryMapper;

    /**
     * 定时任务（每小时）：检查库存低于安全库存 → 生成补货通知 /
     * Scheduled task (hourly): check stock below min safety → generate replenishment notice
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(readOnly = true)
    public void checkReplenishment() {
        List<VmiInventory> lowStockList = vmiInventoryMapper.selectList(
                new LambdaQueryWrapper<VmiInventory>()
                        .lt(VmiInventory::getCurrentStock, VmiInventory::getMinSafetyStock));

        for (VmiInventory vmi : lowStockList) {
            BigDecimal deficit = vmi.getMinSafetyStock().subtract(vmi.getCurrentStock());
            log.warn("VMI补货通知: supplier={} material={} warehouse={} stock={} minSafety={} deficit={}",
                    vmi.getSupplierId(), vmi.getMaterialId(), vmi.getWarehouseCode(),
                    vmi.getCurrentStock(), vmi.getMinSafetyStock(), deficit);
            // TODO: 发送补货通知给供应商 / Send replenishment notification to supplier
        }

        if (!lowStockList.isEmpty()) {
            log.info("VMI补货检查完成: {} 个物料低于安全库存", lowStockList.size());
        }
    }

    /**
     * 定时任务（每小时）：检查库存高于最大库存 → 生成预警 /
     * Scheduled task (hourly): check stock above max → generate alert
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(readOnly = true)
    public void checkOverstock() {
        List<VmiInventory> overstockList = vmiInventoryMapper.selectList(
                new LambdaQueryWrapper<VmiInventory>()
                        .gt(VmiInventory::getCurrentStock, VmiInventory::getMaxStock));

        for (VmiInventory vmi : overstockList) {
            BigDecimal excess = vmi.getCurrentStock().subtract(vmi.getMaxStock());
            log.warn("VMI超库存预警: supplier={} material={} warehouse={} stock={} maxStock={} excess={}",
                    vmi.getSupplierId(), vmi.getMaterialId(), vmi.getWarehouseCode(),
                    vmi.getCurrentStock(), vmi.getMaxStock(), excess);
            // TODO: 发送超库存预警通知 / Send overstock alert notification
        }

        if (!overstockList.isEmpty()) {
            log.info("VMI超库检查完成: {} 个物料超过最大库存", overstockList.size());
        }
    }

    /**
     * 供应商门户端库存查询（按供应商ID） / Supplier portal inventory query (by supplier ID)
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 该供应商所有VMI物料库存 / All VMI inventory for this supplier
     */
    public List<VmiInventory> queryBySupplier(Long supplierId) {
        return vmiInventoryMapper.selectList(
                new LambdaQueryWrapper<VmiInventory>()
                        .eq(VmiInventory::getSupplierId, supplierId));
    }

    /**
     * 采购端库存看板（按仓库维度） / Procurement dashboard (by warehouse)
     *
     * @param warehouseCode 仓库编码 / Warehouse code
     * @param page          当前页 / Current page
     * @param size          每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<VmiInventory> dashboardByWarehouse(String warehouseCode, int page, int size) {
        LambdaQueryWrapper<VmiInventory> wrapper = new LambdaQueryWrapper<>();
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            wrapper.eq(VmiInventory::getWarehouseCode, warehouseCode);
        }
        wrapper.orderByAsc(VmiInventory::getSupplierId)
               .orderByAsc(VmiInventory::getMaterialId);
        return vmiInventoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 采购端库存看板（按物料维度汇总） / Procurement dashboard (aggregated by material)
     *
     * @return 物料维度库存汇总 / Material-level inventory summary
     */
    public List<Map<String, Object>> dashboardByMaterial() {
        List<VmiInventory> all = vmiInventoryMapper.selectList(null);
        return all.stream()
                .collect(Collectors.groupingBy(VmiInventory::getMaterialId))
                .entrySet().stream().map(entry -> {
                    BigDecimal totalStock = entry.getValue().stream()
                            .map(VmiInventory::getCurrentStock)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("materialId", entry.getKey());
                    m.put("totalStock", totalStock);
                    m.put("warehouseCount", entry.getValue().size());
                    return m;
                }).collect(Collectors.toList());
    }

    /**
     * 采购端库存看板（按供应商维度汇总） / Procurement dashboard (aggregated by supplier)
     *
     * @return 供应商维度库存汇总 / Supplier-level inventory summary
     */
    public List<Map<String, Object>> dashboardBySupplier() {
        List<VmiInventory> all = vmiInventoryMapper.selectList(null);
        return all.stream()
                .collect(Collectors.groupingBy(VmiInventory::getSupplierId))
                .entrySet().stream().map(entry -> {
                    BigDecimal totalStock = entry.getValue().stream()
                            .map(VmiInventory::getCurrentStock)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("supplierId", entry.getKey());
                    m.put("totalStock", totalStock);
                    m.put("materialCount", entry.getValue().size());
                    return m;
                }).collect(Collectors.toList());
    }

    /**
     * 创建/更新 VMI 库存记录 / Create or update VMI inventory record
     *
     * @param vmi VMI库存实体 / VMI inventory entity
     * @return 保存后的记录 / Saved record
     */
    @Transactional(rollbackFor = Exception.class)
    public VmiInventory saveOrUpdate(VmiInventory vmi) {
        // 判断是否已存在 / Check if already exists
        VmiInventory existing = vmiInventoryMapper.selectOne(
                new LambdaQueryWrapper<VmiInventory>()
                        .eq(VmiInventory::getSupplierId, vmi.getSupplierId())
                        .eq(VmiInventory::getMaterialId, vmi.getMaterialId())
                        .eq(VmiInventory::getWarehouseCode, vmi.getWarehouseCode()));

        if (existing != null) {
            vmi.setId(existing.getId());
            vmi.setLastUpdateTime(LocalDateTime.now());
            vmiInventoryMapper.updateById(vmi);
        } else {
            vmi.setLastUpdateTime(LocalDateTime.now());
            vmiInventoryMapper.insert(vmi);
        }

        log.info("VMI库存已更新: supplier={} material={} warehouse={} stock={}",
                vmi.getSupplierId(), vmi.getMaterialId(), vmi.getWarehouseCode(), vmi.getCurrentStock());
        return vmi;
    }

    /**
     * 批量获取低于安全库存的物料 / Batch query materials below safety stock
     *
     * @return 低库存列表 / Low stock list
     */
    public List<VmiInventory> getLowStockItems() {
        return vmiInventoryMapper.selectList(
                new LambdaQueryWrapper<VmiInventory>()
                        .lt(VmiInventory::getCurrentStock, VmiInventory::getMinSafetyStock));
    }
}
