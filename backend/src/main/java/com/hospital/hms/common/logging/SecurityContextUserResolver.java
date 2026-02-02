package com.hospital.hms.common.logging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves the current user identifier for MDC logging.
 * Uses SecurityContext (works with Basic auth, JWT, or any Authentication).
 * Returns principal name (e.g. username) when authenticated; never logs sensitive details.
 */
public final class SecurityContextUserResolver {

    private SecurityContextUserResolver() {
    }

    /**
     * Get the current user ID for MDC. Use principal name (username) when authenticated;
     * otherwise ANONYMOUS. Do not log passwords or tokens.
     */
    public static String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
            return MdcKeys.ANONYMOUS_USER;
        }
        String name = auth.getName();
        return (name != null && !name.isBlank()) ? name : MdcKeys.ANONYMOUS_USER;
    }
}
