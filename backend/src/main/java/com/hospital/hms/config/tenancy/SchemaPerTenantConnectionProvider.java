package com.hospital.hms.config.tenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Stoppable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Switches MySQL database per tenant by changing the connection catalog.
 */
@Component
public class SchemaPerTenantConnectionProvider implements MultiTenantConnectionProvider<String>, Stoppable {

    private final DataSource dataSource;

    public SchemaPerTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        String previousCatalog = safeGetCatalog(connection);
        try {
            switchToTenant(connection, tenantIdentifier);
            connection.setClientInfo("hms.previousCatalog", previousCatalog == null ? "" : previousCatalog);
            return connection;
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        String previousCatalog = null;
        try {
            previousCatalog = connection.getClientInfo("hms.previousCatalog");
        } catch (Exception ignored) {
        }
        if (previousCatalog != null && !previousCatalog.isBlank()) {
            try {
                switchToTenant(connection, previousCatalog);
            } catch (SQLException ignored) {
                // If reset fails, we still must close the connection.
            }
        }
        connection.close();
    }

    private void switchToTenant(Connection connection, String tenantIdentifier) throws SQLException {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            return;
        }
        try {
            connection.setCatalog(tenantIdentifier);
        } catch (SQLException setCatalogFailed) {
            try (Statement st = connection.createStatement()) {
                st.execute("USE `" + tenantIdentifier.replace("`", "") + "`");
            }
        }
    }

    private String safeGetCatalog(Connection connection) {
        try {
            return connection.getCatalog();
        } catch (SQLException ignored) {
            return null;
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @Override
    public void stop() {
        // no-op
    }
}

