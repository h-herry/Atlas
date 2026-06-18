package com.atlas.inventory.service;

import com.atlas.inventory.entity.Inventory;
import com.atlas.inventory.entity.InventoryChangeLog;
import com.atlas.inventory.mapper.InventoryChangeLogMapper;
import com.atlas.inventory.mapper.InventoryMapper;
import com.atlas.inventory.model.DeductRequest;
import com.atlas.inventory.model.DeductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("库存服务乐观锁单元测试")
class InventoryServiceTest {

    @Mock private InventoryMapper inventoryMapper;
    @Mock private InventoryChangeLogMapper changeLogMapper;

    @InjectMocks
    private InventoryService inventoryService;

    // ======================== 扣减库存 ========================

    @Test
    @DisplayName("成功扣减库存：version 匹配，扣减成功")
    void testDeductStock_Success() {
        Inventory current = buildInventory(1L, 100L, 1L, new BigDecimal("100"), 0);
        Inventory updated = buildInventory(1L, 100L, 1L, new BigDecimal("70"), 1);

        DeductRequest request = new DeductRequest();
        request.setSkuId(100L);
        request.setWarehouseId(1L);
        request.setQty(new BigDecimal("30"));
        request.setVersion(0);
        request.setOrderNo("PO-001");

        when(inventoryMapper.selectBySkuAndWarehouse(100L, 1L)).thenReturn(current);
        when(inventoryMapper.deductStock(1L, new BigDecimal("30"), 0)).thenReturn(1);
        when(inventoryMapper.selectById(1L)).thenReturn(updated);
        when(changeLogMapper.insert(any(InventoryChangeLog.class))).thenReturn(1);

        DeductResponse response = inventoryService.deduct(request);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getNewStockQty()).isEqualByComparingTo(new BigDecimal("70"));
        assertThat(response.getNewVersion()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("扣减成功");
    }

    // ======================== 乐观锁冲突（重试后成功） ========================

    @Test
    @DisplayName("乐观锁冲突重试后成功：第一次版本冲突，重试后扣减成功")
    void testDeductStock_OptimisticLockFail() {
        Inventory currentV0 = buildInventory(1L, 100L, 1L, new BigDecimal("50"), 0);
        Inventory currentV1 = buildInventory(1L, 100L, 1L, new BigDecimal("20"), 1);

        DeductRequest request = new DeductRequest();
        request.setSkuId(100L);
        request.setWarehouseId(1L);
        request.setQty(new BigDecimal("30"));
        request.setVersion(0);
        request.setOrderNo("PO-002");

        when(inventoryMapper.selectBySkuAndWarehouse(100L, 1L)).thenReturn(currentV0);
        // 第一次扣减版本冲突
        when(inventoryMapper.deductStock(1L, new BigDecimal("30"), 0)).thenReturn(0);

        DeductResponse response = inventoryService.deduct(request);

        // 版本冲突时返回失败
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("版本冲突");

        // 验证没有写入变更日志
        verify(changeLogMapper, never()).insert(any());
    }

    // ======================== 增加库存 ========================

    @Test
    @DisplayName("入库增加库存成功")
    void testAddStock_Success() {
        Inventory current = buildInventory(1L, 100L, 1L, new BigDecimal("50"), 0);
        Inventory updated = buildInventory(1L, 100L, 1L, new BigDecimal("100"), 1);

        when(inventoryMapper.selectBySkuAndWarehouse(100L, 1L)).thenReturn(current);
        when(inventoryMapper.addStock(1L, new BigDecimal("50"), 0)).thenReturn(1);
        when(inventoryMapper.selectById(1L)).thenReturn(updated);
        when(changeLogMapper.insert(any(InventoryChangeLog.class))).thenReturn(1);

        Inventory result = inventoryService.addStock(100L, 1L, new BigDecimal("50"), 1L);

        assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("入库时乐观锁冲突：重试一次后成功")
    void testAddStock_OptimisticLockRetry() {
        Inventory currentV0 = buildInventory(1L, 100L, 1L, new BigDecimal("50"), 0);
        Inventory retryCurrent = buildInventory(1L, 100L, 1L, new BigDecimal("80"), 1);
        Inventory updated = buildInventory(1L, 100L, 1L, new BigDecimal("130"), 2);

        when(inventoryMapper.selectBySkuAndWarehouse(100L, 1L)).thenReturn(currentV0);
        // 第一次 addStock 失败
        when(inventoryMapper.addStock(1L, new BigDecimal("50"), 0)).thenReturn(0);
        // 重试：重查
        when(inventoryMapper.selectById(1L)).thenReturn(retryCurrent);
        // 重试成功
        when(inventoryMapper.addStock(1L, new BigDecimal("50"), 1)).thenReturn(1);
        when(changeLogMapper.insert(any(InventoryChangeLog.class))).thenReturn(1);

        Inventory result = inventoryService.addStock(100L, 1L, new BigDecimal("50"), 1L);

        assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("130"));
        assertThat(result.getVersion()).isEqualTo(2);

        verify(inventoryMapper, times(2)).addStock(anyLong(), any(), anyInt());
        verify(inventoryMapper, times(2)).selectById(1L);
        verify(changeLogMapper).insert(any(InventoryChangeLog.class));
    }

    @Test
    @DisplayName("库存不足：返回 success=false")
    void testDeductStock_InsufficientStock() {
        Inventory current = buildInventory(1L, 100L, 1L, new BigDecimal("10"), 0);

        DeductRequest request = new DeductRequest();
        request.setSkuId(100L);
        request.setWarehouseId(1L);
        request.setQty(new BigDecimal("50"));
        request.setVersion(0);
        request.setOrderNo("PO-003");

        when(inventoryMapper.selectBySkuAndWarehouse(100L, 1L)).thenReturn(current);

        DeductResponse response = inventoryService.deduct(request);

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("库存不足");

        verify(inventoryMapper, never()).deductStock(anyLong(), any(), anyInt());
    }

    // ======================== 辅助方法 ========================

    private Inventory buildInventory(Long id, Long skuId, Long warehouseId, BigDecimal quantity, int version) {
        Inventory inv = new Inventory();
        inv.setId(id);
        inv.setSkuId(skuId);
        inv.setWarehouseId(warehouseId);
        inv.setQuantity(quantity);
        inv.setLockedQty(BigDecimal.ZERO);
        inv.setSafetyStock(BigDecimal.ZERO);
        inv.setVersion(version);
        return inv;
    }
}
