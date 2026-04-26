package com.hospital.hms.config.tenancy;

/**
 * Holds current tenant database (MySQL catalog) for the current request thread.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenant(String tenantDbName) {
        CURRENT_TENANT.set(tenantDbName);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

