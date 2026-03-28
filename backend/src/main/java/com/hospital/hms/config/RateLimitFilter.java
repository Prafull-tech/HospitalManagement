package com.hospital.hms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiter for the /auth/login endpoint.
 * Limits requests per IP within a sliding window. Upgrade to Redis-backed when scaling horizontally.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final int maxAttempts;
    private final ConcurrentMap<String, RateBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${hms.security.rate-limit.login-attempts-per-minute:10}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/api/auth/login".equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        RateBucket bucket = buckets.compute(clientIp, (key, existing) -> {
            if (existing == null || existing.isExpired()) return new RateBucket();
            return existing;
        });

        if (bucket.incrementAndCheck(maxAttempts)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP={} on /auth/login", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"message\":\"Too many login attempts. Please try again later.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateBucket {
        private final Instant windowStart = Instant.now();
        private final AtomicInteger count = new AtomicInteger(0);

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plusSeconds(60));
        }

        boolean incrementAndCheck(int max) {
            return count.incrementAndGet() <= max;
        }
    }
}
