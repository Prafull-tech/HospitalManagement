package com.hospital.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    private final Environment environment;

    @Value("${hms.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Value("${hms.security.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    @Value("${hms.tenant.base-domain:hms.com}")
    private String tenantBaseDomain;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String normalizedTenantBaseDomain = normalizeBaseDomain(tenantBaseDomain);
        String tenantOriginPatterns = "http://*." + normalizedTenantBaseDomain + ":*,https://*." + normalizedTenantBaseDomain;

        String patternsRaw = allowedOriginPatterns;
        if (!StringUtils.hasText(patternsRaw) && Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            patternsRaw = "http://localhost:*,http://127.0.0.1:*,http://192.168.*:*";
        }
        if (StringUtils.hasText(patternsRaw) && !patternsRaw.contains(normalizedTenantBaseDomain)) {
            patternsRaw += "," + tenantOriginPatterns;
        } else if (!StringUtils.hasText(patternsRaw)) {
            patternsRaw = tenantOriginPatterns;
        }

        if (StringUtils.hasText(patternsRaw)) {
            List<String> patterns = Arrays.stream(patternsRaw.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            configuration.setAllowedOriginPatterns(patterns);
        } else {
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            configuration.setAllowedOrigins(origins);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String normalizeBaseDomain(String domain) {
        if (!StringUtils.hasText(domain)) {
            return "hms.com";
        }
        return domain.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\.+", "");
    }
}
