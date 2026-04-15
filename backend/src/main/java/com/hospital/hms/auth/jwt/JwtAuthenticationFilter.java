package com.hospital.hms.auth.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.repository.AppUserRepository;
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
    private final AppUserRepository userRepository;
    private final TenantResolutionService tenantResolutionService;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   AppUserRepository userRepository,
                                   TenantResolutionService tenantResolutionService) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
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
                Long tokenVersion = jwt.getClaim("tokenVersion").isNull() ? null : jwt.getClaim("tokenVersion").asLong();
                AppUser user = username == null ? null : userRepository.findByUsernameIgnoreCase(username).orElse(null);
                if (user != null && Boolean.TRUE.equals(user.getActive()) && tokenVersion != null && tokenVersion.equals(user.getTokenVersion())) {
                    Long hospitalId = user.getHospital() != null ? user.getHospital().getId() : null;
                    Optional<Hospital> tenantHospital = tenantResolutionService.resolveTenantHospital(request);
                    if (tenantHospital.isPresent() && !tenantHospital.get().getId().equals(hospitalId)) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":403,\"message\":\"Tenant host does not match authenticated user\"}");
                        return;
                    }
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                    String hospitalCode = user.getHospital() != null ? user.getHospital().getHospitalCode() : null;
                    String tenantSlug = user.getHospital() != null ? user.getHospital().getSubdomain() : null;
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

