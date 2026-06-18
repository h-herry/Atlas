package com.atlas.inventory.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.inventory.entity.Inventory;
import com.atlas.inventory.entity.InventoryChangeLog;
import com.atlas.inventory.mapper.InventoryChangeLogMapper;
import com.atlas.inventory.mapper.InventoryMapper;
import com.atlas.inventory.model.DeductRequest;
import com.atlas.inventory.model.DeductResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存核心业务服务 / Inventory core business service
 * <p>
 * 提供库存查询、乐观锁扣减、入库增加、低库存预警等功能。
 * 配合 Seata AT 模式的 undo_log 表实现分布式事务。 /
 * Provides inventory query, optimistic-lock deduction, inbound addition, and low-stock alerts.
 * Uses Seata AT mode undo_log table for distributed transactions.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryChangeLogMapper changeLogMapper;

    /** 变动类型常量 / Change type constants */
    public static final int CHANGE_TYPE_PURCHASE_IN = 1;   // 采购入库 / Purchase inbound
    public static final int CHANGE_TYPE_SALE_OUT = 2;       // 销售出库 / Sale outbound
    public static final int CHANGE_TYPE_RETURN_IN = 3;      // 退货入库 / Return inbound
    public static final int CHANGE_TYPE_CHECK = 4;           // 盘点调整 / Inventory check

    /**
     * 按 SKU ID + 仓库ID 查询库存 / Query inventory by SKU ID + warehouse ID
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID / Warehouse ID
     * @return 库存记录 / Inventory record
     */
    public Inventory getBySkuAndWarehouse(Long skuId, Long warehouseId) {
        Inventory inv = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        if (inv == null) {
            throw new BizException(ErrorCode.SKU_NOT_EXIST);
        }
        return inv;
    }

    /**
     * 按主键查询库存 / Query inventory by primary key
     *
     * @param id 库存记录ID / Inventory record ID
     * @return 库存记录 / Inventory record
     */
    public Inventory getById(Long id) {
        Inventory inv = inventoryMapper.selectById(id);
        if (inv == null) {
            throw new BizException(ErrorCode.SKU_NOT_EXIST);
        }
        return inv;
    }

    /**
     * 乐观锁扣减库存 / Optimistic-lock inventory deduction
     * <p>
     * 流程： / Flow:
     * <ol>
     *   <li>查询当前库存，校验库存充足 / Query and validate stock sufficiency</li>
     *   <li>执行乐观锁 UPDATE：WHERE id=? AND version=? / Execute optimistic-lock UPDATE</li>
     *   <li>影响行数为0则抛 OptimisticLockException / 0 rows affected → version conflict</li>
     *   <li>写入变动流水 / Write change log</li>
     * </ol>
     *
     * @param request 扣减请求（含 skuId、warehouseId、qty、version、orderNo） / Deduction request
     * @return 扣减结果 / Deduction result
     */
    @Transactional(rollbackFor = Exception.class)
    public DeductResponse deduct(DeductRequest request) {
        Long skuId = request.getSkuId();
        Long warehouseId = request.getWarehouseId();
        BigDecimal deductQty = request.getQty();

        // 1. 查询当前库存 / Query current inventory
        Inventory current = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        if (current == null) {
            return DeductResponse.builder()
                    .success(false)
                    .message("SKU库存记录不存在")
                    .build();
        }

        // 2. 校验库存充足 / Validate sufficiency
        BigDecimal availableQty = current.getQuantity();
        if (availableQty.compareTo(deductQty) < 0) {
            return DeductResponse.builder()
                    .success(false)
                    .message(String.format("库存不足: 当前=%s, 需要=%s", availableQty, deductQty))
                    .build();
        }

        // 3. 乐观锁扣减 / Optimistic-lock deduction
        int rows = inventoryMapper.deductStock(current.getId(), deductQty, request.getVersion());
        if (rows == 0) {
            return DeductResponse.builder()
                    .success(false)
                    .message("版本冲突，请刷新后重试")
                    .build();
        }

        // 4. 重新查询以获取最新 version / Re-query for updated version
        Inventory updated = inventoryMapper.selectById(current.getId());
        BigDecimal afterQty = updated.getQuantity();

        // 5. 写入变动流水 / Write change log
        InventoryChangeLog changeLog = new InventoryChangeLog();
        changeLog.setSkuId(skuId);
        changeLog.setWarehouseId(warehouseId);
        changeLog.setChangeType(CHANGE_TYPE_SALE_OUT); // 采购场景对库存侧是出库 / Purchase context: outbound for inventory
        changeLog.setChangeQty(deductQty.negate());     // 出库为负数 / Outbound is negative
        changeLog.setBeforeQty(availableQty);
        changeLog.setAfterQty(afterQty);
        changeLog.setOrderNo(request.getOrderNo());
        changeLog.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(changeLog);

        log.info("库存扣减成功: skuId={} warehouseId={} before={} after={} orderNo={}",
                skuId, warehouseId, availableQty, afterQty, request.getOrderNo());

        return DeductResponse.builder()
                .success(true)
                .newStockQty(afterQty)
                .newVersion(updated.getVersion())
                .message("扣减成功")
                .build();
    }

    /**
     * 增加库存（入库） / Add stock (inbound)
     * <p>
     * 同样使用乐观锁 + 变动流水。 / Also uses optimistic lock + change log.
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID / Warehouse ID
     * @param qty         入库数量 / Inbound quantity
     * @param operatorId  操作人ID / Operator ID
     * @return 入库后的库存记录 / Updated inventory record
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory addStock(Long skuId, Long warehouseId, BigDecimal qty, Long operatorId) {
        Inventory current = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        if (current == null) {
            // 不存在则创建新记录 / Create if not exists
            current = new Inventory();
            current.setSkuId(skuId);
            current.setWarehouseId(warehouseId);
            current.setQuantity(BigDecimal.ZERO);
            current.setLockedQty(BigDecimal.ZERO);
            current.setSafetyStock(BigDecimal.ZERO);
            current.setVersion(0);
            inventoryMapper.insert(current);

            // 重新查出带有 id 的完整记录 / Re-query for complete record with ID
            current = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        }

        BigDecimal beforeQty = current.getQuantity();

        // 乐观锁入库 / Optimistic-lock inbound
        int rows = inventoryMapper.addStock(current.getId(), qty, current.getVersion());
        if (rows == 0) {
            // 版本冲突，重查后递归重试一次 / Version conflict, retry once
            Inventory retryCurrent = inventoryMapper.selectById(current.getId());
            int retryRows = inventoryMapper.addStock(retryCurrent.getId(), qty, retryCurrent.getVersion());
            if (retryRows == 0) {
                throw new BizException(5001, "入库版本冲突，请重试");
            }
            current = inventoryMapper.selectById(current.getId());
        } else {
            current = inventoryMapper.selectById(current.getId());
        }

        BigDecimal afterQty = current.getQuantity();

        // 写入变动流水 / Write change log
        InventoryChangeLog changeLog = new InventoryChangeLog();
        changeLog.setSkuId(skuId);
        changeLog.setWarehouseId(warehouseId);
        changeLog.setChangeType(CHANGE_TYPE_PURCHASE_IN);
        changeLog.setChangeQty(qty);
        changeLog.setBeforeQty(beforeQty);
        changeLog.setAfterQty(afterQty);
        changeLog.setOrderNo(null);
        changeLog.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(changeLog);

        log.info("库存入库成功: skuId={} warehouseId={} qty={} before={} after={}",
                skuId, warehouseId, qty, beforeQty, afterQty);

        return current;
    }

    /**
     * 根据订单号批量入库（收货确认回调） / Bulk inbound by order number (receipt confirm callback)
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID / Warehouse ID
     * @param qty         入库数量 / Inbound quantity
     * @param orderNo     订单号 / Order number
     * @param operatorId  操作人 / Operator ID
     * @return 入库后的库存记录 / Updated inventory record
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory addStockByOrderNo(Long skuId, Long warehouseId, BigDecimal qty,
                                        String orderNo, Long operatorId) {
        Inventory current = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        if (current == null) {
            current = new Inventory();
            current.setSkuId(skuId);
            current.setWarehouseId(warehouseId);
            current.setQuantity(BigDecimal.ZERO);
            current.setLockedQty(BigDecimal.ZERO);
            current.setSafetyStock(BigDecimal.ZERO);
            current.setVersion(0);
            inventoryMapper.insert(current);
            current = inventoryMapper.selectBySkuAndWarehouse(skuId, warehouseId);
        }

        BigDecimal beforeQty = current.getQuantity();

        int rows = inventoryMapper.addStock(current.getId(), qty, current.getVersion());
        if (rows == 0) {
            Inventory retryCurrent = inventoryMapper.selectById(current.getId());
            int retryRows = inventoryMapper.addStock(retryCurrent.getId(), qty, retryCurrent.getVersion());
            if (retryRows == 0) {
                throw new BizException(5001, "入库版本冲突，请重试");
            }
            current = inventoryMapper.selectById(current.getId());
        } else {
            current = inventoryMapper.selectById(current.getId());
        }

        BigDecimal afterQty = current.getQuantity();

        InventoryChangeLog changeLog = new InventoryChangeLog();
        changeLog.setSkuId(skuId);
        changeLog.setWarehouseId(warehouseId);
        changeLog.setChangeType(CHANGE_TYPE_PURCHASE_IN);
        changeLog.setChangeQty(qty);
        changeLog.setBeforeQty(beforeQty);
        changeLog.setAfterQty(afterQty);
        changeLog.setOrderNo(orderNo);
        changeLog.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(changeLog);

        log.info("收货入库成功: skuId={} warehouseId={} qty={} orderNo={}", skuId, warehouseId, qty, orderNo);
        return current;
    }

    /**
     * 分页查询库存 / Paginated inventory query
     *
     * @param warehouseId 仓库ID（可选） / Warehouse ID (optional)
     * @param page        当前页 / Current page
     * @param size        每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<Inventory> page(Long warehouseId, int page, int size) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }
        wrapper.orderByDesc(Inventory::getUpdatedAt);
        return inventoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 低库存预警：查询库存量低于安全库存阈值的记录 /
     * Low stock alert: query records where quantity is below safety stock threshold
     *
     * @return 预警列表 / Alert list
     */
    public java.util.List<Inventory> listLowStock() {
        return inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .apply("quantity <= safety_stock")
                        .gt(Inventory::getSafetyStock, 0)
                        .orderByAsc(Inventory::getQuantity));
    }
}
