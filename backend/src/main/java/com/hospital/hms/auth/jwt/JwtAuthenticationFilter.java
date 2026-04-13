package com.hospital.hms.auth.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.tenant.service.TenantResolutionService;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads JWT token from Authorization header and populates SecurityContext.
 * Does not create tokens; only validates and sets user + role.
 * Stores hospital context in authentication details when available.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;
    private final TenantResolutionService tenantResolutionService;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   TenantResolutionService tenantResolutionService) {
        this.tokenService = tokenService;
        this.tenantResolutionService = tenantResolutionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT jwt = tokenService.verify(token);
                String username = jwt.getSubject();
                String role = jwt.getClaim("role").asString();
                if (username != null && role != null) {
                    Long hospitalId = jwt.getClaim("hospitalId").isNull() ? null : jwt.getClaim("hospitalId").asLong();
                    Optional<Hospital> tenantHospital = tenantResolutionService.resolveTenantHospital(request);
                    if (tenantHospital.isPresent() && !tenantHospital.get().getId().equals(hospitalId)) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":403,\"message\":\"Tenant host does not match authenticated user\"}");
                        return;
                    }
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                    String hospitalCode = jwt.getClaim("hospitalCode").isNull() ? null : jwt.getClaim("hospitalCode").asString();
                    String tenantSlug = jwt.getClaim("tenantSlug").isNull() ? null : jwt.getClaim("tenantSlug").asString();
                    if (hospitalId != null || hospitalCode != null || tenantSlug != null) {
                        Map<String, Object> details = new LinkedHashMap<>();
                        details.put("hospitalId", hospitalId);
                        details.put("hospitalCode", hospitalCode);
                        details.put("tenantSlug", tenantSlug);
                        authentication.setDetails(details);
                    }
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    MDC.put(MdcKeys.USER_ID, username);
                }
            } catch (Exception ex) {
                // Invalid / expired token -> clear context; controller layer will return 401
                SecurityContextHolder.clearContext();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.USER_ID);
        }
    }
}

