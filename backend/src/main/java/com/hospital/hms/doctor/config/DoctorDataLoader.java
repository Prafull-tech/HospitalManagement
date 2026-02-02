package com.hospital.hms.doctor.config;

import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Seeds sample departments when none exist (dev only). Safe for H2 and MySQL.
 */
@Configuration
public class DoctorDataLoader {

    @Bean
    @Order(1)
    public ApplicationRunner seedDepartments(MedicalDepartmentRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            String[][] depts = {
                { "CARD", "Cardiology" },
                { "ORTHO", "Orthopedics" },
                { "GEN", "General Medicine" },
                { "PED", "Pediatrics" },
                { "EMER", "Emergency" },
            };
            for (String[] d : depts) {
                MedicalDepartment dept = new MedicalDepartment();
                dept.setCode(d[0]);
                dept.setName(d[1]);
                repo.save(dept);
            }
        };
    }
}
