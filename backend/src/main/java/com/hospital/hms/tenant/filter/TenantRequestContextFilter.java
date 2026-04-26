package com.hospital.hms.tenant.filter;

import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.tenant.context.TenantContextHolder;
import com.hospital.hms.tenant.context.TenantRequestContext;
import com.hospital.hms.tenant.service.TenantResolutionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class TenantRequestContextFilter extends OncePerRequestFilter {

    private final TenantResolutionService tenantResolutionService;

    public TenantRequestContextFilter(TenantResolutionService tenantResolutionService) {
        this.tenantResolutionService = tenantResolutionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            TenantContextHolder.set(buildContext(request));
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private TenantRequestContext buildContext(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long authHospitalId = extractDetail(authentication, "hospitalId", Long.class);
        String authHospitalCode = extractDetail(authentication, "hospitalCode", String.class);
        String authTenantSlug = extractDetail(authentication, "tenantSlug", String.class);
        boolean superAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_SUPER_ADMIN"::equals);

        // Super-admin traffic must always stay on the platform (master) context, even if the request
        // host (or X-HMS-Tenant-Host) points at a tenant domain from a previous session.
        if (superAdmin) {
            return new TenantRequestContext(null, null, null, null, true, false);
        }

        Optional<Hospital> resolvedHospital = tenantResolutionService.resolveTenantHospital(request);
        if (resolvedHospital.isPresent()) {
            Hospital hospital = resolvedHospital.get();
            return new TenantRequestContext(
                    hospital.getId(),
                    hospital.getHospitalCode(),
                    hospital.getSubdomain(),
                    hospital.getTenantDbName(),
                    false,
                    true
            );
        }

        if (!superAdmin && authHospitalId != null) {
            return new TenantRequestContext(
                    authHospitalId,
                    authHospitalCode,
                    authTenantSlug,
                    extractDetail(authentication, "schemaName", String.class),
                    tenantResolutionService.isPlatformHost(request),
                    false
            );
        }

        return new TenantRequestContext(null, null, null, null, tenantResolutionService.isPlatformHost(request), false);
    }

    @SuppressWarnings("unchecked")
    private <T> T extractDetail(Authentication authentication, String key, Class<T> targetType) {
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (!(details instanceof Map<?, ?> detailMap)) {
            return null;
        }
        Object value = detailMap.get(key);
        if (value == null || !targetType.isInstance(value)) {
            return null;
        }
        return (T) value;
    }
}