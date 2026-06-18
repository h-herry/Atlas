package com.atlas.common.aspect;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 审计日志切面 — 拦截带 @AuditLog 注解的方法，记录操作审计 /
 * Audit log aspect — intercepts methods annotated with @AuditLog, records operation audit
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) {
        long start = System.currentTimeMillis();
        String responseStatus = "SUCCESS";

        try {
            Object result = point.proceed();
            return result;
        } catch (Throwable e) {
            responseStatus = "FAILED";
            if (e instanceof RuntimeException re) {
                throw re;
            }
            if (e instanceof Error err) {
                throw err;
            }
            throw new RuntimeException(e);
        } finally {
            long duration = System.currentTimeMillis() - start;
            try {
                com.atlas.common.entity.AuditLog logEntry = auditLogService.build(
                        auditLog.module(),
                        auditLog.operation(),
                        auditLog.description(),
                        point.getArgs(),
                        duration,
                        responseStatus
                );
                auditLogService.record(logEntry);
            } catch (Exception e) {
                log.warn("[AUDIT] 审计日志记录失败: method={}.{}",
                        point.getSignature().getDeclaringTypeName(),
                        point.getSignature().getName(), e);
            }
        }
    }
}
