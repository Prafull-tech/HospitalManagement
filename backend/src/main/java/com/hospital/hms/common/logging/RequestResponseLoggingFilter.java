package com.hospital.hms.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs request method, URI, response status, and elapsed time for every request.
 * Runs at highest precedence so timing is accurate.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
            boolean hasAuthHeader = false;
            try {
                String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                hasAuthHeader = auth != null && !auth.isBlank();
            } catch (Exception ignored) {
                // ignore header access issues
            }

            if (status >= 500) {
                log.error("{} {} -> {} ({}ms) correlationId={} hasAuth={}", method, uri, status, duration, correlationId, hasAuthHeader);
            } else if (status >= 400) {
                log.warn("{} {} -> {} ({}ms) correlationId={} hasAuth={}", method, uri, status, duration, correlationId, hasAuthHeader);
            } else if (duration > 2000) {
                log.warn("SLOW {} {} -> {} ({}ms) correlationId={}", method, uri, status, duration, correlationId);
            } else {
                log.info("{} {} -> {} ({}ms)", method, uri, status, duration);
            }
        }
    }
}
