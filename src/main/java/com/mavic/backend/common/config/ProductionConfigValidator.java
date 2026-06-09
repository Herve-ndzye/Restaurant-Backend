package com.mavic.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("prod")
@Slf4j
public class ProductionConfigValidator {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${admin.invite-code:}")
    private String adminInviteCode;

    @EventListener(ApplicationReadyEvent.class)
    public void validateProductionConfig() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET must be set in production");
        }
        if ("ADMIN-SECRET-2026".equals(adminInviteCode)) {
            log.warn("Default admin invite code is in use. Set ADMIN_INVITE_CODE in production.");
        }
        log.info("Production configuration validated");
    }
}
