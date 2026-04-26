package com.hospital.hms.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway is run programmatically so we can migrate per-tenant databases.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway masterFlyway(
            DataSource dataSource,
            @Value("${hms.flyway.master.locations:classpath:db/migration/master}") String masterLocations
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(masterLocations)
                .baselineOnMigrate(true)
                .load();
    }
}

