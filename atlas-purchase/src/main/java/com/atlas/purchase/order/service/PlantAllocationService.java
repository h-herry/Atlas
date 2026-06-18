package com.atlas.purchase.order.service;

import com.atlas.purchase.order.entity.SupplierPlantRel;
import com.atlas.purchase.order.mapper.SupplierPlantRelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 多工厂分单服务 — 基于工厂+物料自动匹配可供应供应商列表 /
 * Multi-plant allocation service — auto-match available suppliers by plant + material
 * <p>
 * 核心逻辑： /
 * Core logic:
 * <ol>
 *   <li>根据工厂编码查询关联的有效供应商（按优先级+提前期排序） / Query active suppliers by plant code (sorted by priority + lead time)</li>
 *   <li>支持多工厂需求合并拆解：同一物料多工厂需求按供应商产能拆分订单 / Support multi-plant demand consolidation & split by supplier capacity</li>
 *   <li>产能约束校验：分单数量不超过供应商产能上限，超上限分流至备选供应商 / Capacity check: allocation qty not exceeding cap; auto-redirect to backup supplier</li>
 * </ol>
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlantAllocationService {

    private final SupplierPlantRelMapper relMapper;

    /**
     * 根据工厂+物料查询可供应供应商列表 / Query available suppliers by plant + material
     * <p>
     * 按优先级升序、提前期升序排序，过滤禁用关联。 /
     * Sorted by priority asc, lead time asc; filters disabled relations.
     *
     * @param plantCode 工厂编码 / Plant code
     * @return 供应商关联列表（已排序） / Supplier relation list (sorted)
     */
    public List<SupplierPlantRel> getAvailableSuppliers(String plantCode) {
        return relMapper.selectList(
                new LambdaQueryWrapper<SupplierPlantRel>()
                        .eq(SupplierPlantRel::getPlantCode, plantCode)
                        .eq(SupplierPlantRel::getIsActive, 1)
                        .orderByAsc(SupplierPlantRel::getPriority)
                        .orderByAsc(SupplierPlantRel::getLeadTimeDays));
    }

    /**
     * 多工厂需求合并分单 / Multi-plant demand consolidation & order allocation
     * <p>
     * 输入多工厂需求池，按供应商产能和优先级拆分订单。 /
     * Input multi-plant demand pool; outputs allocation plan by supplier capacity and priority.
     *
     * @param demands 多工厂需求列表 / Multi-plant demand list
     * @return 分单结果 / Allocation result
     */
    public AllocationResult allocate(List<PlantDemand> demands) {
        AllocationResult result = new AllocationResult();
        result.setAllocations(new ArrayList<>());
        result.setUnallocated(new ArrayList<>());

        // 按物料分组 / Group by material
        Map<Long, List<PlantDemand>> byMaterial = demands.stream()
                .collect(Collectors.groupingBy(PlantDemand::getMaterialId));

        for (Map.Entry<Long, List<PlantDemand>> entry : byMaterial.entrySet()) {
            Long materialId = entry.getKey();
            List<PlantDemand> materialDemands = entry.getValue();

            for (PlantDemand demand : materialDemands) {
                // 获取该工厂的可用供应商 / Get available suppliers for this plant
                List<SupplierPlantRel> suppliers = getAvailableSuppliers(demand.getPlantCode());

                if (suppliers.isEmpty()) {
                    // 无可供应供应商 → 记录为未分配 / No available supplier → mark as unallocated
                    UnallocatedDemand unallocated = new UnallocatedDemand();
                    unallocated.setPlantCode(demand.getPlantCode());
                    unallocated.setMaterialId(materialId);
                    unallocated.setRequiredQty(demand.getRequiredQty());
                    unallocated.setReason("Factory has no active supplier for material");
                    result.getUnallocated().add(unallocated);
                    continue;
                }

                // 按产能分配 / Allocate by capacity
                BigDecimal remainingQty = demand.getRequiredQty();
                for (SupplierPlantRel supplier : suppliers) {
                    if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }

                    BigDecimal cap = supplier.getCapacityPerMonth();
                    BigDecimal allocQty = (cap != null && cap.compareTo(remainingQty) < 0)
                            ? cap : remainingQty;

                    AllocationItem item = new AllocationItem();
                    item.setPlantCode(demand.getPlantCode());
                    item.setMaterialId(materialId);
                    item.setSupplierId(supplier.getSupplierId());
                    item.setPriority(supplier.getPriority());
                    item.setLeadTimeDays(supplier.getLeadTimeDays());
                    item.setAllocatedQty(allocQty);
                    result.getAllocations().add(item);

                    remainingQty = remainingQty.subtract(allocQty);

                    log.info("分单: plant={} material={} supplier={} qty={}",
                            demand.getPlantCode(), materialId, supplier.getSupplierId(), allocQty);
                }

                // 产能不足仍有余量 / Remaining qty when all suppliers exhausted
                if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                    UnallocatedDemand unallocated = new UnallocatedDemand();
                    unallocated.setPlantCode(demand.getPlantCode());
                    unallocated.setMaterialId(materialId);
                    unallocated.setRequiredQty(remainingQty);
                    unallocated.setReason("Suppliers capacity exhausted, remaining " + remainingQty);
                    result.getUnallocated().add(unallocated);
                }
            }
        }

        return result;
    }

    /**
     * 校验产能约束 / Validate capacity constraint
     * <p>
     * 检查供应商-工厂维度已分单总量是否超过月度产能上限。 /
     * Checks whether total allocated quantity exceeds monthly capacity cap for a supplier-plant pair.
     *
     * @param supplierId 供应商ID / Supplier ID
     * @param plantCode  工厂编码 / Plant code
     * @param newQty     新增分单数量 / New allocation quantity
     * @return true 表示未超产能 / true means within capacity
     */
    public boolean validateCapacity(Long supplierId, String plantCode, BigDecimal newQty,
                                     BigDecimal existingAllocated) {
        SupplierPlantRel rel = relMapper.selectOne(
                new LambdaQueryWrapper<SupplierPlantRel>()
                        .eq(SupplierPlantRel::getSupplierId, supplierId)
                        .eq(SupplierPlantRel::getPlantCode, plantCode)
                        .eq(SupplierPlantRel::getIsActive, 1));

        if (rel == null || rel.getCapacityPerMonth() == null) {
            return true; // 无产能上限 → 视为可分配 / No cap → treat as allocatable
        }

        BigDecimal totalAfter = existingAllocated.add(newQty);
        return totalAfter.compareTo(rel.getCapacityPerMonth()) <= 0;
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * 工厂需求 / Plant demand
     */
    @Data
    public static class PlantDemand {
        /** 工厂编码 / Plant code */
        private String plantCode;
        /** 物料ID / Material ID */
        private Long materialId;
        /** 需求数量 / Required quantity */
        private BigDecimal requiredQty;
    }

    /**
     * 分单结果 / Allocation result
     */
    @Data
    public static class AllocationResult {
        /** 已分配项列表 / Allocated items */
        private List<AllocationItem> allocations;
        /** 未分配项列表 / Unallocated items */
        private List<UnallocatedDemand> unallocated;
    }

    /**
     * 分单项 / Allocation item
     */
    @Data
    public static class AllocationItem {
        private String plantCode;
        private Long materialId;
        private Long supplierId;
        private Integer priority;
        private Integer leadTimeDays;
        private BigDecimal allocatedQty;
    }

    /**
     * 未分配需求 / Unallocated demand
     */
    @Data
    public static class UnallocatedDemand {
        private String plantCode;
        private Long materialId;
        private BigDecimal requiredQty;
        private String reason;
    }
}
