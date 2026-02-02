package com.hospital.hms.ward.config;

import com.hospital.hms.ward.entity.WardTypeMaster;
import com.hospital.hms.ward.repository.WardTypeMasterRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Seeds default ward types when none exist (General, Semi Pvt, Private, ICU, Emergency).
 * Safe for H2 and MySQL.
 */
@Configuration
public class WardTypeMasterDataLoader {

    private static final List<String> DEFAULT_NAMES = List.of(
            "General",
            "Semi Pvt",
            "Private",
            "ICU",
            "Emergency"
    );

    @Bean
    @Order(4)
    public ApplicationRunner seedWardTypeMaster(WardTypeMasterRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            for (String name : DEFAULT_NAMES) {
                WardTypeMaster entity = new WardTypeMaster();
                entity.setName(name);
                entity.setIsActive(true);
                repository.save(entity);
            }
        };
    }
}
