package com.aicoup.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA BaseEntity 설정 관련 Config
 */
@EnableJpaAuditing(
        auditorAwareRef = "requestedByAuditorAware",
        dateTimeProviderRef = "dateTimeProvider"
)
@Configuration
public class JpaAuditConfiguration {
}
