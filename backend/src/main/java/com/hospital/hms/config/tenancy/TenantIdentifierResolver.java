package com.hospital.hms.config.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides tenant identifier to Hibernate per session.
 * In MySQL, this maps to the database/catalog name.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private final String defaultTenantDbName;

    public TenantIdentifierResolver(
            @Value("${hms.tenancy.default-db:${MYSQL_DATABASE:hms}}") String defaultTenantDbName
    ) {
        this.defaultTenantDbName = defaultTenantDbName;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        return (tenant == null || tenant.isBlank()) ? defaultTenantDbName : tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

