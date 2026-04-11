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
 * <p>
 * If a user already exists but the stored BCrypt hash does not match the expected dev password,
 * the hash is reset so demo logins (e.g. admin / admin123) work after DB drift.
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
                    new DevUser("superadmin", "Super Admin", "super123", UserRole.SUPER_ADMIN),
                    new DevUser("pharm", "Pharmacy Manager", "pharm123", UserRole.PHARMACY_MANAGER),
                    new DevUser("pharmacist", "Pharmacist", "pharm123", UserRole.PHARMACIST),
                    new DevUser("store", "Store In-charge", "store123", UserRole.STORE_INCHARGE),
                    new DevUser("ipdph", "IPD Pharmacist", "ipdph123", UserRole.IPD_PHARMACIST),
                    new DevUser("doctor", "Doctor", "doctor123", UserRole.DOCTOR),
                    new DevUser("nurse", "Nurse", "nurse123", UserRole.NURSE),
                    new DevUser("ipd", "IPD Manager", "ipd123", UserRole.IPD_MANAGER),
                    new DevUser("bill", "Billing", "bill123", UserRole.BILLING),
                    new DevUser("quality", "Quality Manager", "quality123", UserRole.QUALITY_MANAGER),
                    new DevUser("labtech", "Lab Technician", "lab123", UserRole.LAB_TECHNICIAN),
                    new DevUser("labsup", "Lab Supervisor", "lab123", UserRole.LAB_SUPERVISOR),
                    new DevUser("pathologist", "Pathologist", "lab123", UserRole.PATHOLOGIST),
                    new DevUser("radtech", "Radiology Tech", "rad123", UserRole.RADIOLOGY_TECH),
                    new DevUser("bloodtech", "Blood Bank Tech", "blood123", UserRole.BLOOD_BANK_TECH),
                    new DevUser("reception", "Receptionist", "rec123", UserRole.RECEPTIONIST)
            );

            for (DevUser u : users) {
                repo.findByUsernameIgnoreCase(u.username()).ifPresentOrElse(existing -> {
                    boolean changed = false;
                    if (!encoder.matches(u.rawPassword(), existing.getPasswordHash())) {
                        existing.setPasswordHash(encoder.encode(u.rawPassword()));
                        changed = true;
                        log.warn("Dev: reset password hash for username={} (did not match demo password)", u.username());
                    }
                    if (existing.getRole() != u.role()) {
                        existing.setRole(u.role());
                        changed = true;
                    }
                    if (!u.fullName().equals(existing.getFullName())) {
                        existing.setFullName(u.fullName());
                        changed = true;
                    }
                    if (!Boolean.TRUE.equals(existing.getActive())) {
                        existing.setActive(true);
                        changed = true;
                    }
                    if (changed) {
                        repo.save(existing);
                        log.info("Dev: updated user username={} role={}", u.username(), existing.getRole());
                    }
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
