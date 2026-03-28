package com.hospital.hms.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HmsHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public HmsHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long patientCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM patients", Long.class);
            return Health.up()
                    .withDetail("database", "MySQL reachable")
                    .withDetail("patientCount", patientCount)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("database", "MySQL unreachable")
                    .withException(ex)
                    .build();
        }
    }
}
