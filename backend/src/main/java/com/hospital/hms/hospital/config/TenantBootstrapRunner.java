package com.hospital.hms.hospital.config;

import com.hospital.hms.config.tenancy.TenantContext;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.service.TenantProvisioningService;
import com.hospital.hms.tenant.entity.TenantUser;
import com.hospital.hms.tenant.repository.TenantUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Ensures every hospital row has a provisioned tenant database with migrations applied,
 * and seeds a minimal set of tenant users so hospital login works out of the box in dev.
 */
@Configuration
public class TenantBootstrapRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantBootstrapRunner.class);

    @Bean
    @Order(10)
    public ApplicationRunner bootstrapTenants(HospitalRepository hospitalRepository,
                                              TenantProvisioningService tenantProvisioningService,
                                              TenantUserRepository tenantUserRepository,
                                              PasswordEncoder passwordEncoder) {
        return args -> {
            List<Hospital> hospitals = hospitalRepository.findAll();
            for (Hospital hospital : hospitals) {
                if (Boolean.TRUE.equals(hospital.getDeleted())) {
                    continue;
                }
                backfillTenantDbName(hospital, hospitalRepository);
                try {
                    tenantProvisioningService.provisionTenant(hospital);
                } catch (Exception ex) {
                    log.warn("Failed to provision tenant db for hospital id={} name={}: {}",
                            hospital.getId(), hospital.getHospitalName(), ex.getMessage());
                    continue;
                }
                seedDefaultTenantUsers(hospital, tenantUserRepository, passwordEncoder);
            }
        };
    }

    private void backfillTenantDbName(Hospital hospital, HospitalRepository hospitalRepository) {
        if (hospital.getTenantDbName() != null && !hospital.getTenantDbName().isBlank()) {
            return;
        }
        String derived = deriveTenantDbName(hospital.getSubdomain(), hospital.getHospitalCode());
        hospital.setTenantDbName(derived);
        hospitalRepository.save(hospital);
        log.info("Back-filled tenantDbName={} for hospital id={}", derived, hospital.getId());
    }

    @Transactional
    protected void seedDefaultTenantUsers(Hospital hospital,
                                          TenantUserRepository tenantUserRepository,
                                          PasswordEncoder passwordEncoder) {
        String tenantDb = hospital.getTenantDbName();
        if (tenantDb == null || tenantDb.isBlank()) {
            return;
        }
        try {
            TenantContext.setCurrentTenant(tenantDb);
            upsertTenantUser(tenantUserRepository, passwordEncoder,
                    "admin@" + safeSubdomain(hospital) + ".local",
                    "Hospital Admin", "hospital_admin", "Admin@123");
            if ("genius36".equalsIgnoreCase(hospital.getSubdomain())) {
                upsertTenantUser(tenantUserRepository, passwordEncoder,
                        "doctor101", "Dr. Default", "doctor", "Doct@123");
            }
        } finally {
            TenantContext.clear();
        }
    }

    private void upsertTenantUser(TenantUserRepository repo,
                                  PasswordEncoder passwordEncoder,
                                  String email,
                                  String name,
                                  String role,
                                  String plainPassword) {
        repo.findByEmailIgnoreCase(email).ifPresentOrElse(existing -> {
            // Leave existing users untouched so ops-set passwords aren't reverted.
        }, () -> {
            TenantUser user = new TenantUser();
            user.setId(UUID.randomUUID().toString());
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            user.setPasswordHash(passwordEncoder.encode(plainPassword));
            user.setActive(true);
            repo.save(user);
            log.info("Seeded tenant user email={} role={}", email, role);
        });
    }

    private String safeSubdomain(Hospital hospital) {
        if (hospital.getSubdomain() != null && !hospital.getSubdomain().isBlank()) {
            return hospital.getSubdomain().toLowerCase(Locale.ROOT);
        }
        if (hospital.getHospitalCode() != null) {
            return hospital.getHospitalCode().toLowerCase(Locale.ROOT);
        }
        return "hospital";
    }

    private String deriveTenantDbName(String subdomain, String hospitalCode) {
        String source = subdomain != null ? subdomain : hospitalCode;
        source = source != null ? source.trim().toLowerCase(Locale.ROOT) : "hospital";
        source = source.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (source.isBlank()) {
            source = "hospital";
        }
        String db = "hosp_" + source;
        if (db.length() > 100) {
            db = db.substring(0, 100);
        }
        return db;
    }
}
