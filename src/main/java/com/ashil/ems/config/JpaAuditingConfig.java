package com.ashil.ems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA auditing so @CreatedDate / @LastModifiedDate
 * fields are populated automatically on persist and update.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
