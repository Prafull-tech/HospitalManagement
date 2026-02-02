package com.hospital.hms.ipd.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * IPD-specific data seeding when needed. Ward/bed data is seeded by WardDataLoader.
 */
@Configuration
public class IPDDataLoader {

    @Bean
    @Order(10)
    public ApplicationRunner ipdDataLoader() {
        return args -> {
            // IPD-specific seeds (e.g. sample admissions) can be added here.
            // Wards and beds are seeded by WardDataLoader.
        };
    }
}
