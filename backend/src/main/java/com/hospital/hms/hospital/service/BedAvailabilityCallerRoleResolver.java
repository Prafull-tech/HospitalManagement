package com.hospital.hms.hospital.service;

import com.hospital.hms.hospital.config.BedAvailabilityRoles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Resolves the current user's role for Bed Availability operations from Spring Security context.
 * Uses {@link BedAvailabilityRoles} constants only; no hardcoded role strings.
 */
@Component
public class BedAvailabilityCallerRoleResolver {

    /**
     * Resolves caller role from authentication. Prefer ADMIN, then IPD_MANAGER, then DOCTOR.
     * Returns DOCTOR if unauthenticated or no matching role (safe default for read-only).
     */
    public BedAvailabilityCallerRole resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return BedAvailabilityCallerRole.DOCTOR;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) {
            return BedAvailabilityCallerRole.DOCTOR;
        }
        for (GrantedAuthority a : authorities) {
            String authority = a.getAuthority();
            String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
            if (BedAvailabilityRoles.ADMIN.equals(role)) {
                return BedAvailabilityCallerRole.ADMIN;
            }
            if (BedAvailabilityRoles.IPD_MANAGER.equals(role)) {
                return BedAvailabilityCallerRole.IPD_MANAGER;
            }
            if (BedAvailabilityRoles.DOCTOR.equals(role)) {
                return BedAvailabilityCallerRole.DOCTOR;
            }
        }
        return BedAvailabilityCallerRole.DOCTOR;
    }
}
