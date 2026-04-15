package com.hospital.hms.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs schema migrations before other startup logic (e.g. DevUserDataLoader).
 * Applies critical column expansions for environments where Flyway is disabled.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureUserRoleColumnCapacity();
        ensureHospitalLogoColumnCapacity();
    }

    private void ensureUserRoleColumnCapacity() {
        try {
            jdbcTemplate.execute("ALTER TABLE hms_users MODIFY COLUMN role VARCHAR(50) NOT NULL");
            log.info("Schema migration: hms_users.role column expanded to VARCHAR(50)");
        } catch (Exception e) {
            // Table may not exist yet (fresh DB) or column already correct.
            log.debug("Schema migration skipped for hms_users.role: {}", e.getMessage());
        }
    }

    private void ensureHospitalLogoColumnCapacity() {
        try {
            jdbcTemplate.execute("ALTER TABLE hospitals MODIFY COLUMN logo_url LONGTEXT");
            log.info("Schema migration: hospitals.logo_url column expanded to LONGTEXT");
        } catch (Exception e) {
            // Table may not exist yet (fresh DB) or column already correct.
            log.debug("Schema migration skipped for hospitals.logo_url: {}", e.getMessage());
        }
    }
}
