package com.mavic.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @AfterReturning(value = "@annotation(auditLog)", returning = "result")
    public void logAfterSuccess(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        logAudit(joinPoint, auditLog, "SUCCESS", null);
    }

    @AfterThrowing(value = "@annotation(auditLog)", throwing = "exception")
    public void logAfterFailure(JoinPoint joinPoint, AuditLog auditLog, Exception exception) {
        logAudit(joinPoint, auditLog, "FAILURE", exception.getMessage());
    }

    private void logAudit(JoinPoint joinPoint, AuditLog auditLog, String status, String errorMessage) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null && authentication.isAuthenticated()) 
                    ? authentication.getName() 
                    : "anonymous";

            String action = auditLog.action().isEmpty() 
                    ? joinPoint.getSignature().getName() 
                    : auditLog.action();

            String ipAddress = getClientIP();
            String method = joinPoint.getSignature().toShortString();
            
            // Log audit information
            if ("SUCCESS".equals(status)) {
                log.info("AUDIT | Timestamp: {} | User: {} | IP: {} | Action: {} | Method: {} | Status: {}",
                        LocalDateTime.now(), username, ipAddress, action, method, status);
            } else {
                log.warn("AUDIT | Timestamp: {} | User: {} | IP: {} | Action: {} | Method: {} | Status: {} | Error: {}",
                        LocalDateTime.now(), username, ipAddress, action, method, status, errorMessage);
            }
            
            // In production, you would save this to a database table for audit trail
            // Example: auditRepository.save(new AuditEntry(...));
            
        } catch (Exception e) {
            log.error("Failed to log audit information", e);
        }
    }

    private String getClientIP() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader == null) {
                    return request.getRemoteAddr();
                }
                return xfHeader.split(",")[0];
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return "unknown";
    }
}
