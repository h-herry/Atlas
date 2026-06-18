package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.mapper.SupplierBlacklistMapper;
import com.atlas.supplier.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 供应商主服务 — 集成黑名单拦截校验 + Redis 缓存字典数据 /
 * Supplier main service — integrated blacklist intercept check + Redis cache for dictionary data
 *
 * @author atlas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;
    private final SupplierBlacklistMapper blacklistMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "supplier:page:";
    private static final long CACHE_TTL_MINUTES = 5;

    /**
     * 分页查询供应商（带 Redis 缓存，TTL 5min） /
     * Paginated query of suppliers (with Redis cache, TTL 5 min)
     */
    @SuppressWarnings("unchecked")
    public Page<Supplier> page(String keyword, int page, int size) {
        String cacheKey = CACHE_KEY_PREFIX + (StringUtils.hasText(keyword) ? keyword : "_all")
                + ":" + page + ":" + size;

        // 先从缓存读取 / Check cache first
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("供应商分页缓存命中: key={}", cacheKey);
            return (Page<Supplier>) cached;
        }

        // 缓存未命中，查 DB / Cache miss, query DB
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Supplier::getSupplierName, keyword)
                   .or().like(Supplier::getSupplierNo, keyword);
        }
        wrapper.orderByDesc(Supplier::getCreatedAt);
        Page<Supplier> result = supplierMapper.selectPage(new Page<>(page, size), wrapper);

        // 写入缓存 / Write to cache
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("供应商分页缓存写入: key={}", cacheKey);
        return result;
    }

    /**
     * 根据ID查询供应商 / Query supplier by ID
     */
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    /**
     * 新增供应商 — 注册前黑名单拦截，并清除相关缓存 /
     * Add supplier — blacklist intercept before registration, and evict related cache
     */
    @CacheEvict(value = "supplier:info", key = "#supplier.id")
    public boolean save(Supplier supplier) {
        if (supplier.getId() != null && isBlacklisted(supplier.getId())) {
            throw new BizException(ErrorCode.SUPPLIER_BLACKLISTED);
        }
        boolean result = supplierMapper.insert(supplier) > 0;
        if (result) {
            evictSupplierPageCache();
        }
        return result;
    }

    /**
     * 更新供应商，并清除相关缓存 / Update supplier and evict related cache
     */
    public boolean update(Supplier supplier) {
        boolean result = supplierMapper.updateById(supplier) > 0;
        if (result) {
            evictSupplierPageCache();
        }
        return result;
    }

    /**
     * 删除供应商，并清除相关缓存 / Delete supplier and evict related cache
     */
    public boolean delete(Long id) {
        boolean result = supplierMapper.deleteById(id) > 0;
        if (result) {
            evictSupplierPageCache();
        }
        return result;
    }

    /**
     * 校验供应商是否在黑名单中 — 供采购/招标等外部模块调用 /
     * Check if supplier is blacklisted — for external modules such as procurement, bidding
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return true 表示在黑名单中 / true if blacklisted
     */
    public boolean isBlacklisted(Long supplierId) {
        return blacklistMapper.existsActiveBySupplierId(supplierId);
    }

    /**
     * 清除供应商分页缓存（按前缀模糊删除） /
     * Evict supplier pagination cache (wildcard delete by prefix)
     */
    private void evictSupplierPageCache() {
        try {
            var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("供应商分页缓存已清除: count={}", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除供应商分页缓存失败", e);
        }
    }
}
