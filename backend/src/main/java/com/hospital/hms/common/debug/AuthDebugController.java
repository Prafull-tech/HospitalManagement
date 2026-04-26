package com.hospital.hms.common.debug;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/_debug")
public class AuthDebugController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> auth(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> out = new LinkedHashMap<>();

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        out.put("hasAuthorizationHeader", authHeader != null && !authHeader.isBlank());
        out.put("authorizationPrefix", authHeader == null ? null : authHeader.substring(0, Math.min(authHeader.length(), 12)));

        out.put("authenticationPresent", auth != null);
        out.put("authenticated", auth != null && auth.isAuthenticated());
        out.put("principal", auth == null ? null : auth.getPrincipal());
        out.put("name", auth == null ? null : auth.getName());
        out.put("detailsType", auth == null || auth.getDetails() == null ? null : auth.getDetails().getClass().getName());
        out.put("details", auth == null ? null : auth.getDetails());
        List<String> authorities = auth == null
                ? List.of()
                : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        out.put("authorities", authorities);
        return ResponseEntity.ok(out);
    }
}

