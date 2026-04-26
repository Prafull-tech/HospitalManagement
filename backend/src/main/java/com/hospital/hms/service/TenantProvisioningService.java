package com.hospital.hms.service;

import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.net.URI;
import java.util.List;

@Service
public class TenantProvisioningService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource masterDataSource;
    private final HospitalRepository hospitalRepository;
    private final String datasourceUrl;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String tenantLocations;

    public TenantProvisioningService(
            JdbcTemplate jdbcTemplate,
            DataSource masterDataSource,
            HospitalRepository hospitalRepository,
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String datasourceUsername,
            @Value("${spring.datasource.password}") String datasourcePassword,
            @Value("${hms.flyway.tenant.locations:classpath:db/migration/tenant}") String tenantLocations
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.masterDataSource = masterDataSource;
        this.hospitalRepository = hospitalRepository;
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.tenantLocations = tenantLocations;
    }

    @Transactional
    public Hospital provisionTenant(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));
        if (hospital.getTenantDbName() == null || hospital.getTenantDbName().isBlank()) {
            throw new IllegalStateException("Hospital is missing tenantDbName");
        }
        provisionTenant(hospital);
        return hospital;
    }

    public void provisionTenant(Hospital hospital) {
        String tenantDb = hospital.getTenantDbName();
        createDatabaseIfNotExists(tenantDb);
        migrateTenant(tenantDb);
    }

    public List<String> migrateAllTenants() {
        return hospitalRepository.findByDeletedFalseAndIsActiveTrueOrderByHospitalNameAsc()
                .stream()
                .map(h -> {
                    migrateTenant(h.getTenantDbName());
                    return h.getTenantDbName();
                })
                .toList();
    }

    public void migrateMaster(Flyway masterFlyway) {
        masterFlyway.migrate();
    }

    private void createDatabaseIfNotExists(String tenantDb) {
        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + tenantDb.replace("`", "") + "`");
    }

    private void migrateTenant(String tenantDb) {
        String tenantUrl = replaceDatabaseInJdbcUrl(datasourceUrl, tenantDb);
        Flyway.configure()
                .dataSource(tenantUrl, datasourceUsername, datasourcePassword)
                .locations(tenantLocations)
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }

    /**
     * Replaces the database segment of a MySQL JDBC URL.\n
     * Example: jdbc:mysql://localhost:3306/hms?x=y -> jdbc:mysql://localhost:3306/tenant?x=y
     */
    static String replaceDatabaseInJdbcUrl(String jdbcUrl, String database) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalArgumentException("Unsupported JDBC URL: " + jdbcUrl);
        }
        String raw = jdbcUrl.substring("jdbc:".length());
        URI uri = URI.create(raw);
        String path = uri.getPath();
        String newPath = "/" + database;
        if (path == null || path.isBlank() || "/".equals(path)) {
            newPath = "/" + database;
        } else {
            newPath = "/" + database;
        }
        URI rebuilt = URI.create("mysql://" + uri.getAuthority() + newPath + (uri.getQuery() != null ? "?" + uri.getQuery() : ""));
        return "jdbc:" + rebuilt.toString();
    }
}

