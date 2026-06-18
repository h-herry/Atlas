package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.mapper.SupplierBlacklistMapper;
import com.atlas.supplier.mapper.SupplierMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("供应商服务单元测试")
class SupplierServiceTest {

    @Mock private SupplierMapper supplierMapper;
    @Mock private SupplierBlacklistMapper blacklistMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SupplierService supplierService;

    // ======================== 创建供应商 ========================

    @Test
    @DisplayName("创建供应商：未在黑名单中，创建成功")
    void testCreateSupplier() {
        Supplier supplier = buildSupplier("SUP001", "测试供应商有限公司");

        when(blacklistMapper.existsActiveBySupplierId(null)).thenReturn(false);
        when(supplierMapper.insert(supplier)).thenReturn(1);
        when(redisTemplate.keys(anyString())).thenReturn(null);

        boolean result = supplierService.save(supplier);

        assertThat(result).isTrue();
        verify(supplierMapper).insert(supplier);
        verify(blacklistMapper).existsActiveBySupplierId(null);
    }

    // ======================== 黑名单校验 ========================

    @Test
    @DisplayName("黑名单校验：供应商不在黑名单中，校验通过")
    void testCheckBlacklist() {
        when(blacklistMapper.existsActiveBySupplierId(100L)).thenReturn(false);

        boolean isBlacklisted = supplierService.isBlacklisted(100L);

        assertThat(isBlacklisted).isFalse();
        verify(blacklistMapper).existsActiveBySupplierId(100L);
    }

    @Test
    @DisplayName("黑名单中供应商保存被拒绝：抛 BizException(SUPPLIER_BLACKLISTED)")
    void testSaveSupplierInBlacklist() {
        Supplier supplier = buildSupplier("SUP002", "黑名单企业");
        supplier.setId(200L);

        when(blacklistMapper.existsActiveBySupplierId(200L)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.save(supplier))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.SUPPLIER_BLACKLISTED.getCode());

        verify(supplierMapper, never()).insert(any());
    }

    // ======================== 缓存清除 ========================

    @Test
    @DisplayName("更新供应商后清除分页缓存")
    void testUpdateSupplier_EvictsCache() {
        Supplier supplier = buildSupplier("SUP001", "更新后的名称");
        supplier.setId(1L);

        when(supplierMapper.updateById(supplier)).thenReturn(1);
        when(redisTemplate.keys(startsWith("supplier:page:"))).thenReturn(
                Set.of("supplier:page:_all:1:10", "supplier:page:测试:1:10"));
        when(redisTemplate.delete(anySet())).thenReturn(2L);

        boolean result = supplierService.update(supplier);

        assertThat(result).isTrue();
        verify(redisTemplate).keys(startsWith("supplier:page:"));
        verify(redisTemplate).delete(anySet());
    }

    // ======================== 辅助方法 ========================

    private Supplier buildSupplier(String supplierNo, String name) {
        Supplier s = new Supplier();
        s.setSupplierNo(supplierNo);
        s.setSupplierName(name);
        s.setContactPerson("张三");
        s.setContactPhone("13800138000");
        s.setEmail("test@supplier.com");
        s.setSupplierType(1);
        s.setStatus(1);
        return s;
    }
}
