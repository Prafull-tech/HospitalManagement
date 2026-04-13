package com.hospital.hms.tenant.context;

public class TenantRequestContext {

    private final Long hospitalId;
    private final String hospitalCode;
    private final String tenantSlug;
    private final boolean platformRequest;
    private final boolean resolvedFromHost;

    public TenantRequestContext(Long hospitalId,
                                String hospitalCode,
                                String tenantSlug,
                                boolean platformRequest,
                                boolean resolvedFromHost) {
        this.hospitalId = hospitalId;
        this.hospitalCode = hospitalCode;
        this.tenantSlug = tenantSlug;
        this.platformRequest = platformRequest;
        this.resolvedFromHost = resolvedFromHost;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public boolean isPlatformRequest() {
        return platformRequest;
    }

    public boolean isResolvedFromHost() {
        return resolvedFromHost;
    }

    public boolean hasHospital() {
        return hospitalId != null;
    }
}