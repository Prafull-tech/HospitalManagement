package com.hospital.hms.system.config;

import com.hospital.hms.system.entity.*;
import com.hospital.hms.system.repository.FeatureToggleRepository;
import com.hospital.hms.system.repository.RoleModulePermissionRepository;
import com.hospital.hms.system.repository.SystemModuleRepository;
import com.hospital.hms.system.repository.SystemRoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Seeds system roles, modules, default permissions, and feature toggles when empty.
 * DB-agnostic (H2 & MySQL). Safe to run on every startup (idempotent).
 */
@Configuration
public class SystemDataLoader {

    private static final String[] ROLE_CODES = {
            "ADMIN", "SUPER_ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE", "LAB_TECH",
            "PHARMACIST", "BILLING", "HR", "IT_ADMIN", "HELP_DESK"
    };

    private static final String[] ROLE_NAMES = {
            "Administrator", "Super Admin", "Receptionist", "Doctor", "Nurse", "Lab Technician",
            "Pharmacist", "Billing", "Human Resources", "IT Admin", "Help Desk"
    };

    @Bean
    @Order(100)
    public ApplicationRunner seedSystemData(SystemRoleRepository roleRepo,
                                            SystemModuleRepository moduleRepo,
                                            RoleModulePermissionRepository permRepo,
                                            FeatureToggleRepository featureRepo) {
        return args -> {
            if (roleRepo.count() > 0) return;

            for (int i = 0; i < ROLE_CODES.length; i++) {
                SystemRole r = new SystemRole();
                r.setCode(ROLE_CODES[i]);
                r.setName(ROLE_NAMES[i]);
                r.setSystemRole(true);
                r.setActive(true);
                r.setSortOrder(i + 1);
                roleRepo.save(r);
            }

            SystemModule reception = saveModule(moduleRepo, "RECEPTION", "Reception", ModuleCategory.FRONT_OFFICE, "/reception", 1);
            SystemModule opd = saveModule(moduleRepo, "OPD", "OPD", ModuleCategory.CLINICAL, "/opd", 2);
            SystemModule ipd = saveModule(moduleRepo, "IPD", "IPD", ModuleCategory.CLINICAL, "/ipd", 3);
            SystemModule doctors = saveModule(moduleRepo, "DOCTORS", "Doctors / Medical Staff", ModuleCategory.CLINICAL, "/doctors", 4);
            SystemModule nursing = saveModule(moduleRepo, "NURSING", "Nursing", ModuleCategory.NURSING, "/nursing", 5);
            SystemModule wards = saveModule(moduleRepo, "WARDS", "Wards & Beds", ModuleCategory.CLINICAL, "/wards", 6);
            SystemModule system = saveModule(moduleRepo, "SYSTEM_CONFIG", "System Configuration", ModuleCategory.SYSTEM, "/admin/config", 90);

            SystemRole admin = roleRepo.findByCode("ADMIN").orElseThrow();
            SystemRole superAdmin = roleRepo.findByCode("SUPER_ADMIN").orElseThrow();
            SystemRole receptionist = roleRepo.findByCode("RECEPTIONIST").orElseThrow();
            SystemRole doctor = roleRepo.findByCode("DOCTOR").orElseThrow();
            SystemRole nurse = roleRepo.findByCode("NURSE").orElseThrow();

            assignAll(permRepo, admin, List.of(reception, opd, ipd, doctors, nursing, wards, system));
            assignAll(permRepo, superAdmin, List.of(reception, opd, ipd, doctors, nursing, wards, system));
            assignAll(permRepo, receptionist, List.of(reception, opd));
            assignAll(permRepo, doctor, List.of(reception, opd, ipd, doctors, nursing, wards));
            assignAll(permRepo, nurse, List.of(ipd, nursing, wards));

            saveFeature(featureRepo, "EMERGENCY_OT", "Emergency OT", "Enable emergency OT module", false, 1);
            saveFeature(featureRepo, "TELEMEDICINE", "Telemedicine", "Enable telemedicine consultations", false, 2);
            saveFeature(featureRepo, "ONLINE_PAYMENTS", "Online Payments", "Enable online payment gateway", false, 3);
        };
    }

    private SystemModule saveModule(SystemModuleRepository repo, String code, String name,
                                    ModuleCategory category, String routePath, int sortOrder) {
        SystemModule m = new SystemModule();
        m.setCode(code);
        m.setName(name);
        m.setModuleCategory(category);
        m.setRoutePath(routePath);
        m.setEnabled(true);
        m.setSortOrder(sortOrder);
        return repo.save(m);
    }

    private void assignAll(RoleModulePermissionRepository permRepo, SystemRole role, List<SystemModule> modules) {
        for (SystemModule module : modules) {
            for (ActionType action : ActionType.values()) {
                RoleModulePermission rmp = new RoleModulePermission();
                rmp.setRole(role);
                rmp.setModule(module);
                rmp.setActionType(action);
                rmp.setVisibility(ModuleVisibility.VISIBLE);
                permRepo.save(rmp);
            }
        }
    }

    private void saveFeature(FeatureToggleRepository repo, String key, String name, String desc, boolean enabled, int sortOrder) {
        FeatureToggle f = new FeatureToggle();
        f.setFeatureKey(key);
        f.setName(name);
        f.setDescription(desc);
        f.setEnabled(enabled);
        f.setSortOrder(sortOrder);
        repo.save(f);
    }
}
