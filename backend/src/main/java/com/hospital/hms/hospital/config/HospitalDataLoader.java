package com.hospital.hms.hospital.config;

import com.hospital.hms.hospital.entity.BedAvailability;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.BedAvailabilityRepository;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Seeds default hospital and sample bed availability when empty (dev). Safe for H2 and MySQL.
 * Uses WardType enum (no external dependency).
 */
@Configuration
public class HospitalDataLoader {

    private static final List<WardType> WARD_TYPES = List.of(WardType.GENERAL, WardType.SEMI_PRIVATE, WardType.PRIVATE, WardType.ICU, WardType.EMERGENCY);
    private static final int[] TOTAL = { 20, 10, 5, 8, 6 };
    private static final int[] OCCUPIED = { 12, 4, 2, 5, 3 };
    private static final int[] RESERVED = { 1, 0, 0, 1, 0 };
    private static final int[] UNDER_CLEANING = { 2, 1, 0, 0, 1 };

    @Bean
    @Order(6)
    public ApplicationRunner seedHospitalsAndBedAvailability(HospitalRepository hospitalRepo,
                                                             BedAvailabilityRepository bedAvailabilityRepo) {
        return args -> {
            if (hospitalRepo.count() > 0) return;
            Hospital h = new Hospital();
            h.setHospitalCode("MAIN");
            h.setHospitalName("Main Hospital");
            h.setLocation("Central Campus");
            h.setIsActive(true);
            h.setDeleted(false);
            h = hospitalRepo.save(h);

            for (int i = 0; i < WARD_TYPES.size(); i++) {
                BedAvailability ba = new BedAvailability();
                ba.setHospital(h);
                ba.setWardType(WARD_TYPES.get(i));
                ba.setTotalBeds(TOTAL[i]);
                ba.setOccupiedBeds(OCCUPIED[i]);
                ba.setReservedBeds(RESERVED[i]);
                ba.setUnderCleaningBeds(UNDER_CLEANING[i]);
                bedAvailabilityRepo.save(ba);
            }
        };
    }
}
