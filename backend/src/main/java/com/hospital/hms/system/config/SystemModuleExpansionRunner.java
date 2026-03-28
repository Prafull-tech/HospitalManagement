package com.hospital.hms.system.config;

import com.hospital.hms.system.entity.*;
import com.hospital.hms.system.repository.RoleModulePermissionRepository;
import com.hospital.hms.system.repository.SystemModuleRepository;
import com.hospital.hms.system.repository.SystemRoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Idempotent expansion: adds clinical/ops {@link SystemModule} rows missing from early seeds
 * and grants default {@link RoleModulePermission} rows when a role has no permissions for that module.
 * Safe on every startup; does not remove or overwrite existing matrix entries.
 */
@Configuration
public class SystemModuleExpansionRunner {

    @Bean
    @Order(101)
    public ApplicationRunner expandModulesAndDefaultPermissions(
            SystemModuleRepository moduleRepo,
            SystemRoleRepository roleRepo,
            RoleModulePermissionRepository permRepo) {
        return args -> {
            SystemModule pharmacy = ensureModule(moduleRepo, "PHARMACY", "Pharmacy", ModuleCategory.PHARMACY, "/pharmacy", 10);
            SystemModule lab = ensureModule(moduleRepo, "LAB", "Laboratory", ModuleCategory.DIAGNOSTICS, "/lab", 11);
            SystemModule radiology = ensureModule(moduleRepo, "RADIOLOGY", "Radiology", ModuleCategory.DIAGNOSTICS, "/radiology", 12);
            SystemModule billing = ensureModule(moduleRepo, "BILLING", "Billing", ModuleCategory.BILLING, "/billing", 13);
            SystemModule housekeeping = ensureModule(moduleRepo, "HOUSEKEEPING", "Housekeeping & Services", ModuleCategory.ADMIN, "/housekeeping", 14);
            SystemModule hr = ensureModule(moduleRepo, "HR", "Human Resources", ModuleCategory.ADMIN, "/hr", 15);

            List<SystemModule> clinicalCross = List.of(pharmacy, lab, radiology);

            List<SystemModule> allModules = moduleRepo.findAll();
            for (SystemRole role : roleRepo.findAll()) {
                String code = role.getCode();
                if ("ADMIN".equals(code) || "SUPER_ADMIN".equals(code) || "IT_ADMIN".equals(code)) {
                    for (SystemModule m : allModules) {
                        assignAllActionsIfMissing(permRepo, role, m);
                    }
                }
            }

            Map<String, List<String>> roleToModuleCodes = new LinkedHashMap<>();
            roleToModuleCodes.put("PHARMACIST", List.of("PHARMACY"));
            roleToModuleCodes.put("LAB_TECH", List.of("LAB"));
            roleToModuleCodes.put("BILLING", List.of("BILLING"));
            roleToModuleCodes.put("RECEPTIONIST", List.of("RECEPTION", "OPD"));
            roleToModuleCodes.put("FRONT_DESK", List.of("RECEPTION", "OPD"));
            roleToModuleCodes.put("HELP_DESK", List.of("RECEPTION"));
            roleToModuleCodes.put("HOUSEKEEPING", List.of("HOUSEKEEPING"));
            roleToModuleCodes.put("HR", List.of("HR"));

            for (Map.Entry<String, List<String>> e : roleToModuleCodes.entrySet()) {
                SystemRole role = roleRepo.findByCode(e.getKey()).orElse(null);
                if (role == null) {
                    continue;
                }
                for (String mc : e.getValue()) {
                    moduleRepo.findByCode(mc).ifPresent(m -> assignAllActionsIfMissing(permRepo, role, m));
                }
            }

            roleRepo.findByCode("DOCTOR").ifPresent(r -> {
                for (SystemModule m : clinicalCross) {
                    assignAllActionsIfMissing(permRepo, r, m);
                }
            });
            roleRepo.findByCode("NURSE").ifPresent(r -> {
                for (SystemModule m : clinicalCross) {
                    assignAllActionsIfMissing(permRepo, r, m);
                }
            });
        };
    }

    private SystemModule ensureModule(SystemModuleRepository repo, String code, String name,
                                      ModuleCategory category, String routePath, int sortOrder) {
        return repo.findByCode(code).orElseGet(() -> {
            SystemModule m = new SystemModule();
            m.setCode(code);
            m.setName(name);
            m.setModuleCategory(category);
            m.setRoutePath(routePath);
            m.setEnabled(true);
            m.setSortOrder(sortOrder);
            return repo.save(m);
        });
    }

    private void assignAllActionsIfMissing(RoleModulePermissionRepository permRepo, SystemRole role, SystemModule module) {
        List<RoleModulePermission> existing = permRepo.findByRoleIdAndModuleId(role.getId(), module.getId());
        if (!existing.isEmpty()) {
            return;
        }
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
