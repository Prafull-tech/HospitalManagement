package com.hospital.hms.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Request-scoped MDC filter: Correlation ID and User ID.
 * Runs once per request (after authentication); reads or generates correlation ID,
 * extracts user from SecurityContext, puts both into MDC, and clears MDC in finally.
 * Registered in SecurityConfig after BasicAuthenticationFilter.
 */
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(MdcKeys.HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            } else {
                correlationId = correlationId.trim();
            }
            MDC.put(MdcKeys.CORRELATION_ID, correlationId);
            response.setHeader(MdcKeys.HEADER_CORRELATION_ID, correlationId);

            String userId = SecurityContextUserResolver.resolveUserId();
            MDC.put(MdcKeys.USER_ID, userId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.USER_ID);
            MDC.remove(MdcKeys.MODULE);
        }
    }
}
