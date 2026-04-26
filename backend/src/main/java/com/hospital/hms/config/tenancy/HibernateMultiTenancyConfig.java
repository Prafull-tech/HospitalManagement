package com.hospital.hms.config.tenancy;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateMultiTenancyConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            SchemaPerTenantConnectionProvider connectionProvider,
            TenantIdentifierResolver tenantIdentifierResolver
    ) {
        return (Map<String, Object> props) -> {
            props.put("hibernate.multi_tenant_connection_provider", connectionProvider);
            props.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
            props.put("hibernate.multiTenancy", "SCHEMA");
        };
    }
}

