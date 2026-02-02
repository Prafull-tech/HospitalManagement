package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.config.AdmissionPriorityOverrideRoles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * Resolves whether the current caller is an authority allowed to override admission priority.
 * Returns the caller's username for audit logging when allowed.
 */
@Component
public class AdmissionPriorityOverrideAuthorityResolver {

    /**
     * If the caller has one of the authority roles (MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER),
     * returns the caller's username for audit. Otherwise returns empty.
     */
    public Optional<String> resolveAuthorityUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) {
            return Optional.empty();
        }
        for (GrantedAuthority a : authorities) {
            String authority = a.getAuthority();
            String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
            if (AdmissionPriorityOverrideRoles.OVERRIDE_AUTHORITY_ROLES.contains(role)) {
                String name = authentication.getName();
                return Optional.of(name != null && !name.isBlank() ? name : "unknown");
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true if the caller has any of the authority roles that may override priority.
     */
    public boolean hasOverrideAuthority(Authentication authentication) {
        return resolveAuthorityUsername(authentication).isPresent();
    }
}
