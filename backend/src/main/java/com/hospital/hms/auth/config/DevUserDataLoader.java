package com.hospital.hms.auth.config;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Seeds development/test users when running with 'dev' profile.
 * NEVER active in production.
 */
@Configuration
@Profile("dev")
public class DevUserDataLoader {

    private static final Logger log = LoggerFactory.getLogger(DevUserDataLoader.class);

    @Bean
    CommandLineRunner seedDevUsers(AppUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            List<DevUser> users = List.of(
                    new DevUser("admin", "Admin", "admin123", UserRole.ADMIN),
                    new DevUser("pharm", "Pharmacy Manager", "pharm123", UserRole.PHARMACY_MANAGER),
                    new DevUser("store", "Store In-charge", "store123", UserRole.STORE_INCHARGE),
                    new DevUser("ipdph", "IPD Pharmacist", "ipdph123", UserRole.IPD_PHARMACIST),
                    new DevUser("doctor", "Doctor", "doctor123", UserRole.DOCTOR),
                    new DevUser("nurse", "Nurse", "nurse123", UserRole.NURSE),
                    new DevUser("ipd", "IPD Manager", "ipd123", UserRole.IPD_MANAGER),
                    new DevUser("bill", "Billing", "bill123", UserRole.BILLING),
                    new DevUser("quality", "Quality Manager", "quality123", UserRole.QUALITY_MANAGER)
            );

            for (DevUser u : users) {
                repo.findByUsernameIgnoreCase(u.username()).ifPresentOrElse(existing -> {
                    // keep existing; do not overwrite passwords
                }, () -> {
                    AppUser user = new AppUser();
                    user.setUsername(u.username());
                    user.setFullName(u.fullName());
                    user.setPasswordHash(encoder.encode(u.rawPassword()));
                    user.setRole(u.role());
                    user.setActive(true);
                    repo.save(user);
                    log.info("Seeded dev user username={} role={}", u.username(), u.role());
                });
            }
        };
    }

    private record DevUser(String username, String fullName, String rawPassword, UserRole role) {}
}

