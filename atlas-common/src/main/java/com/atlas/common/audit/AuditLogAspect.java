package com.atlas.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 审计日志切面 — 拦截 @AuditLog 注解，记录操作日志 /
 * Audit log aspect — intercepts @AuditLog annotation, records operation logs
 * <p>
 * 生产建议：将日志发送到 MQ 异步落库（阶段三实现） /
 * Production recommendation: send logs to MQ for async persistence (Phase 3)
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String username = getCurrentUsername();
        String ip = getClientIp();

        // 前置日志 / Pre-execution log
        if (auditLog.recordParams()) {
            log.info("[AUDIT] user={} ip={} module={} action={} params={}",
                    username, ip, auditLog.module(), auditLog.action(),
                    Arrays.toString(joinPoint.getArgs()));
        }

        // 执行目标方法 / Execute target method
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("[AUDIT] user={} module={} action={} ERROR: {}",
                    username, auditLog.module(), auditLog.action(), ex.getMessage());
            throw ex;
        }

        long cost = System.currentTimeMillis() - start;
        if (auditLog.recordResult()) {
            log.info("[AUDIT] user={} module={} action={} cost={}ms result={}",
                    username, auditLog.module(), auditLog.action(), cost, result);
        } else {
            log.info("[AUDIT] user={} module={} action={} cost={}ms",
                    username, auditLog.module(), auditLog.action(), cost);
        }
        return result;
    }

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
