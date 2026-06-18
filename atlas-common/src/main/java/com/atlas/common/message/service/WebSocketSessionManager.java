package com.atlas.common.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 会话管理器 — 管理在线/离线状态 /
 * WebSocket session manager — manages online/offline status
 *
 * <p>维护 supplierId ↔ 在线 sessionId 集合的映射，用于判断供应商是否在线以及定向推送 /
 * Maintains supplierId ↔ online sessionId set mapping for determining online status and targeted push</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** supplierId → sessionId 集合 / supplierId → set of sessionIds */
    private final Map<Long, Set<String>> supplierSessions = new ConcurrentHashMap<>();

    /** sessionId → supplierId 反向映射 / sessionId → supplierId reverse mapping */
    private final Map<String, Long> sessionToSupplier = new ConcurrentHashMap<>();

    /** sessionId → userId 反向映射 / sessionId → userId reverse mapping */
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    /** userId → sessionId 集合 / userId → set of sessionIds */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    // ==================== 在线管理 / Online management ====================

    /**
     * 注册供应商上线 / Register supplier online
     */
    public void supplierOnline(Long supplierId, String sessionId) {
        supplierSessions.computeIfAbsent(supplierId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionToSupplier.put(sessionId, supplierId);
        log.info("供应商上线: supplierId={}, sessionId={}", supplierId, sessionId);
    }

    /**
     * 注册用户上线 / Register user online
     */
    public void userOnline(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionToUser.put(sessionId, userId);
        log.debug("用户上线: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 处理下线 / Handle offline
     */
    public void offline(String sessionId) {
        // 移除供应商会话 / Remove supplier session
        Long supplierId = sessionToSupplier.remove(sessionId);
        if (supplierId != null) {
            Set<String> sessions = supplierSessions.get(supplierId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    supplierSessions.remove(supplierId);
                }
            }
            log.info("供应商下线: supplierId={}, sessionId={}", supplierId, sessionId);
        }

        // 移除用户会话 / Remove user session
        Long userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.debug("用户下线: userId={}, sessionId={}", userId, sessionId);
        }
    }

    // ==================== 状态查询 / Status queries ====================

    /**
     * 判断供应商是否在线 / Check if supplier is online
     */
    public boolean isSupplierOnline(Long supplierId) {
        Set<String> sessions = supplierSessions.get(supplierId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 判断用户是否在线 / Check if user is online
     */
    public boolean isUserOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取供应商的在线会话数 / Get supplier's online session count
     */
    public int getSupplierSessionCount(Long supplierId) {
        Set<String> sessions = supplierSessions.get(supplierId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * 获取在线供应商总数 / Get total online suppliers
     */
    public int getOnlineSupplierCount() {
        return supplierSessions.size();
    }

    /**
     * 获取在线用户总数 / Get total online users
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
}
