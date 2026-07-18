package com.cinebook.common.config;

import com.cinebook.common.security.CustomerUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Resolves the "current user" for created_by/updated_by columns.
     * Returns empty for anonymous/system actions (e.g. Flyway seed data,
     * scheduler jobs) so those columns stay NULL instead of throwing.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof CustomerUserDetails principal)) {
                return Optional.empty();
            }
            return Optional.ofNullable(principal.getUserId());
        };
    }
}
