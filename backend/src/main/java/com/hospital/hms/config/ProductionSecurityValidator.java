package com.hospital.hms.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails fast on insecure defaults when {@code spring.profiles.active} includes {@code prod}.
 */
@Component
@Profile("prod")
public class ProductionSecurityValidator implements InitializingBean {

    private static final String DEV_PLACEHOLDER_SECRET = "dev-jwt-secret-change-me";

    private final Environment environment;

    public ProductionSecurityValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        String jwt = environment.getProperty("hms.security.jwt.secret", "");
        if (jwt.isBlank() || DEV_PLACEHOLDER_SECRET.equals(jwt.trim())) {
            throw new IllegalStateException(
                    "Production requires a strong JWT secret: set JWT_SECRET or HMS_JWT_SECRET (not the dev placeholder).");
        }
        String dbPassword = environment.getProperty("spring.datasource.password", "");
        if (dbPassword.isBlank()) {
            throw new IllegalStateException("Production requires MYSQL_PASSWORD (or spring.datasource.password) to be set.");
        }
    }
}
