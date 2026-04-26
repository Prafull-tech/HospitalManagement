package com.hospital.hms.auth;

import com.hospital.hms.config.tenancy.TenantContext;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.tenant.service.TenantResolutionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TenantResolutionService tenantResolutionService;

    public JwtFilter(JwtUtil jwtUtil, TenantResolutionService tenantResolutionService) {
        this.jwtUtil = jwtUtil;
        this.tenantResolutionService = tenantResolutionService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Jws<Claims> parsed = jwtUtil.parse(token);
                Claims claims = parsed.getBody();

                String role = claims.get("role", String.class);
                String schemaName = claims.get("schemaName", String.class);
                Object hospitalIdClaim = claims.get("hospitalId");
                Long hospitalId = toLong(hospitalIdClaim);
                boolean isSuperAdmin = Boolean.TRUE.equals(claims.get("isSuperAdmin", Boolean.class))
                        || (role != null && role.equalsIgnoreCase("SUPER_ADMIN"));
                String effectiveRole = (role != null && !role.isBlank())
                        ? role
                        : (isSuperAdmin ? "SUPER_ADMIN" : null);

                Optional<Hospital> resolvedHost = tenantResolutionService.resolveTenantHospital(request);
                if (resolvedHost.isPresent() && !isSuperAdmin) {
                    Hospital hostHospital = resolvedHost.get();
                    if (hospitalId == null || !hostHospital.getId().equals(hospitalId)) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":403,\"message\":\"Tenant host does not match authenticated user\"}");
                        return;
                    }
                }

                if (schemaName != null && !schemaName.isBlank()) {
                    TenantContext.setCurrentTenant(schemaName);
                }

                if (effectiveRole != null && !effectiveRole.isBlank()) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + effectiveRole.toUpperCase());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, java.util.List.of(authority));

                    Map<String, Object> details = new LinkedHashMap<>();
                    if (hospitalId != null) details.put("hospitalId", hospitalId);
                    if (schemaName != null) details.put("schemaName", schemaName);
                    resolvedHost.ifPresent(hospital -> {
                        details.put("hospitalCode", hospital.getHospitalCode());
                        details.put("tenantSlug", hospital.getSubdomain());
                    });
                    authentication.setDetails(details);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    MDC.put(MdcKeys.USER_ID, claims.getSubject());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.USER_ID);
            TenantContext.clear();
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
