package com.atlas.common.security.aspect;

import com.atlas.common.mapper.SysPermissionMapper;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 功能权限校验切面 — 动态从数据库校验，支持通配符与 Redis 缓存 /
 * Permission validation aspect — dynamic DB-based validation with wildcard support and Redis caching
 * <p>
 * 工作流程 / Workflow：
 * <ol>
 *   <li>拦截 @RequirePermission 注解的 Controller 方法</li>
 *   <li>从 SecurityContext 获取当前用户ID</li>
 *   <li>从 Redis 缓存读取用户权限列表（缓存30分钟，miss时查DB回填）</li>
 *   <li>支持通配符匹配：message:* 可匹配 message:view、message:update 等</li>
 *   <li>@RequirePermission(logical=AND) 需同时满足所有权限，OR 满足任一即可</li>
 * </ol>
 * <p>
 * 缓存策略 / Cache Strategy：
 * <ul>
 *   <li>Key: atlas:perm:user:{userId}</li>
 *   <li>TTL: 30 minutes</li>
 *   <li>角色变更时通过事件清理缓存 / Cache eviction on role change via events</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private static final String PERM_CACHE_PREFIX = "atlas:perm:user:";
    private static final long PERM_CACHE_TTL_MINUTES = 30;

    private final SysPermissionMapper sysPermissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 环绕拦截 @RequirePermission 注解的方法 / Around advice for @RequirePermission annotated methods
     */
    @Around("@annotation(com.atlas.common.security.annotation.RequirePermission)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        // 获取当前用户 / Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户未认证 / User not authenticated");
        }

        Long userId = extractUserId(auth);
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "无法获取用户ID / Cannot extract user ID");
        }

        // 加载用户权限列表（优先缓存） / Load user permissions (cache-first)
        List<String> userPermissions = getUserPermissions(userId);

        // 校验权限 / Validate permissions
        String requiredPerm = annotation.value();
        boolean permitted = checkPermission(userPermissions, requiredPerm, annotation.logical());

        if (!permitted) {
            log.warn("权限拒绝: userId={}, required={}, method={}.{}",
                    userId, requiredPerm, method.getDeclaringClass().getSimpleName(), method.getName());
            throw new BizException(ErrorCode.FORBIDDEN,
                    "权限不足，需要: " + requiredPerm + " / Insufficient permissions, required: " + requiredPerm);
        }

        log.debug("权限通过: userId={}, required={}, method={}.{}",
                userId, requiredPerm, method.getDeclaringClass().getSimpleName(), method.getName());
        return joinPoint.proceed();
    }

    /**
     * 校验权限 — 支持通配符和 AND/OR 逻辑 /
     * Validate permission — supports wildcards and AND/OR logic
     *
     * @param userPermissions 用户拥有的权限列表 / List of user's permissions
     * @param required        要求的权限标识（支持 * 通配符） / Required permission code (supports * wildcard)
     * @param logical         AND/OR 逻辑 / AND/OR logic
     * @return true 表示有权限 / true if permitted
     */
    private boolean checkPermission(List<String> userPermissions, String required,
                                     RequirePermission.Logical logical) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        // 超级管理员拥有所有权限 / Super admin has all permissions
        if (userPermissions.contains("*") || userPermissions.contains("SUPER_ADMIN")) {
            return true;
        }

        // 分割多个权限标识（逗号分隔） / Split multiple permission codes (comma-separated)
        String[] requiredPerms = required.split(",");

        return switch (logical) {
            case AND -> {
                // 所有权限都必须满足 / All permissions must be satisfied
                for (String perm : requiredPerms) {
                    if (!matchPermission(userPermissions, perm.trim())) {
                        yield false;
                    }
                }
                yield true;
            }
            case OR -> {
                // 任一权限满足即可 / Any permission satisfied is enough
                for (String perm : requiredPerms) {
                    if (matchPermission(userPermissions, perm.trim())) {
                        yield true;
                    }
                }
                yield false;
            }
        };
    }

    /**
     * 通配符权限匹配 / Wildcard permission matching
     * <p>
     * 支持模式 / Supported patterns：
     * <ul>
     *   <li>精确匹配：message:view → message:view</li>
     *   <li>模块通配：message:* → message:view, message:update, message:manage</li>
     *   <li>全局通配：* → 所有权限</li>
     * </ul>
     *
     * @param userPermissions 用户权限列表 / User permissions list
     * @param required        要求的权限模式 / Required permission pattern
     * @return true 表示匹配 / true if matched
     */
    private boolean matchPermission(List<String> userPermissions, String required) {
        // 精确匹配 / Exact match
        if (userPermissions.contains(required)) {
            return true;
        }

        // 通配符匹配 / Wildcard matching
        if (required.contains("*")) {
            String regex = required
                    .replace(".", "\\.")
                    .replace("*", ".*");
            Pattern pattern = Pattern.compile("^" + regex + "$");
            for (String userPerm : userPermissions) {
                if (pattern.matcher(userPerm).matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取用户权限列表（缓存优先） / Get user permissions (cache-first)
     *
     * @param userId 用户ID / User ID
     * @return 权限标识码列表 / List of permission codes
     */
    @SuppressWarnings("unchecked")
    private List<String> getUserPermissions(Long userId) {
        String cacheKey = PERM_CACHE_PREFIX + userId;

        // 1. 尝试从 Redis 缓存读取 / Try Redis cache
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?> list && !list.isEmpty()) {
                log.debug("命中权限缓存: userId={} / Permission cache hit: userId={}", userId, userId);
                return (List<String>) list;
            }
        } catch (Exception e) {
            log.warn("Redis 权限缓存读取异常，降级查DB: userId={} / Redis cache read error, fallback to DB: userId={}", userId, e);
        }

        // 2. 缓存未命中，查询数据库 / Cache miss, query database
        List<String> permissions = sysPermissionMapper.selectByUserId(userId);
        if (permissions == null) {
            permissions = Collections.emptyList();
        }

        // 3. 回填缓存 / Backfill cache
        try {
            redisTemplate.opsForValue().set(cacheKey, permissions, PERM_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("权限缓存回填: userId={}, count={} / Permission cache backfill: userId={}, count={}",
                    userId, permissions.size(), userId, permissions.size());
        } catch (Exception e) {
            log.warn("Redis 权限缓存回填失败: userId={} / Redis cache backfill failed: userId={}", userId, e);
        }

        return permissions;
    }

    /**
     * 从 Authentication 中提取用户ID / Extract user ID from Authentication
     */
    private Long extractUserId(Authentication auth) {
        try {
            // 尝试从 principal 获取 / Try to get from principal
            if (auth.getPrincipal() instanceof Number num) {
                return num.longValue();
            }
            // 尝试从 JWT Claims details 获取 / Try to get from JWT Claims details
            if (auth.getDetails() instanceof io.jsonwebtoken.Claims claims) {
                return Long.valueOf(claims.getSubject());
            }
            // 兜底：尝试 Long.parse / Fallback: try Long.parse
            return Long.valueOf(auth.getPrincipal().toString());
        } catch (Exception e) {
            log.error("提取用户ID失败 / Failed to extract user ID", e);
            return null;
        }
    }

    /**
     * 清除用户权限缓存（角色变更时调用） / Evict user permission cache (called on role change)
     *
     * @param userId 用户ID / User ID
     */
    public void evictCache(Long userId) {
        try {
            redisTemplate.delete(PERM_CACHE_PREFIX + userId);
            log.info("权限缓存已清除: userId={} / Permission cache evicted: userId={}", userId, userId);
        } catch (Exception e) {
            log.error("清除权限缓存失败: userId={} / Failed to evict permission cache: userId={}", userId, e);
        }
    }
}
