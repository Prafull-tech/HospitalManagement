package com.hospital.hms.tenant.dto;

public class TenantContextResponseDto {
    private String host;
    private boolean platformHost;
    private boolean tenantResolved;
    private Long hospitalId;
    private String hospitalCode;
    private String hospitalName;
    private String tenantSlug;
    private String customDomain;
    private String resolvedBy;
    private String domainVerificationStatus;
    private String certificateStatus;
    private String certificateExpiresAt;
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
    private Boolean active;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public boolean isPlatformHost() { return platformHost; }
    public void setPlatformHost(boolean platformHost) { this.platformHost = platformHost; }
    public boolean isTenantResolved() { return tenantResolved; }
    public void setTenantResolved(boolean tenantResolved) { this.tenantResolved = tenantResolved; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }
    public String getCustomDomain() { return customDomain; }
    public void setCustomDomain(String customDomain) { this.customDomain = customDomain; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getDomainVerificationStatus() { return domainVerificationStatus; }
    public void setDomainVerificationStatus(String domainVerificationStatus) { this.domainVerificationStatus = domainVerificationStatus; }
    public String getCertificateStatus() { return certificateStatus; }
    public void setCertificateStatus(String certificateStatus) { this.certificateStatus = certificateStatus; }
    public String getCertificateExpiresAt() { return certificateExpiresAt; }
    public void setCertificateExpiresAt(String certificateExpiresAt) { this.certificateExpiresAt = certificateExpiresAt; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}