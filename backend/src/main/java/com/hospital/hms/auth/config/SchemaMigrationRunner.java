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
 * Fixes "Data truncated for column 'role'" by expanding hms_users.role to VARCHAR(50).
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
        try {
            jdbcTemplate.execute("ALTER TABLE hms_users MODIFY COLUMN role VARCHAR(50) NOT NULL");
            log.info("Schema migration: hms_users.role column expanded to VARCHAR(50)");
        } catch (Exception e) {
            // Table may not exist yet (fresh DB) or column already correct
            log.debug("Schema migration skipped or already applied: {}", e.getMessage());
        }
    }
}
