package com.hospital.hms.hospital.dto;

/**
 * Hospital response for list and get-by-id. API exposes "active" for the active flag.
 */
public class HospitalResponseDto {

    private Long id;
    private String hospitalCode;
    private String hospitalName;
    private String location;
    private String subdomain;
    private String customDomain;
    private String domainVerificationToken;
    private String domainVerificationStatus;
    private String domainVerifiedAt;
    private String certificateStatus;
    private String certificateRequestedAt;
    private String certificateIssuedAt;
    private String certificateExpiresAt;
    private String lastDomainVerificationError;
    private String lastCertificateError;
    private String logoUrl;
    private String websiteUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String contactEmail;
    private String billingEmail;
    private String contactPhone;
    private String onboardingStatus;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public String getDomainVerificationToken() {
        return domainVerificationToken;
    }

    public void setDomainVerificationToken(String domainVerificationToken) {
        this.domainVerificationToken = domainVerificationToken;
    }

    public String getDomainVerificationStatus() {
        return domainVerificationStatus;
    }

    public void setDomainVerificationStatus(String domainVerificationStatus) {
        this.domainVerificationStatus = domainVerificationStatus;
    }

    public String getDomainVerifiedAt() {
        return domainVerifiedAt;
    }

    public void setDomainVerifiedAt(String domainVerifiedAt) {
        this.domainVerifiedAt = domainVerifiedAt;
    }

    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(String certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public String getCertificateRequestedAt() {
        return certificateRequestedAt;
    }

    public void setCertificateRequestedAt(String certificateRequestedAt) {
        this.certificateRequestedAt = certificateRequestedAt;
    }

    public String getCertificateIssuedAt() {
        return certificateIssuedAt;
    }

    public void setCertificateIssuedAt(String certificateIssuedAt) {
        this.certificateIssuedAt = certificateIssuedAt;
    }

    public String getCertificateExpiresAt() {
        return certificateExpiresAt;
    }

    public void setCertificateExpiresAt(String certificateExpiresAt) {
        this.certificateExpiresAt = certificateExpiresAt;
    }

    public String getLastDomainVerificationError() {
        return lastDomainVerificationError;
    }

    public void setLastDomainVerificationError(String lastDomainVerificationError) {
        this.lastDomainVerificationError = lastDomainVerificationError;
    }

    public String getLastCertificateError() {
        return lastCertificateError;
    }

    public void setLastCertificateError(String lastCertificateError) {
        this.lastCertificateError = lastCertificateError;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getOnboardingStatus() {
        return onboardingStatus;
    }

    public void setOnboardingStatus(String onboardingStatus) {
        this.onboardingStatus = onboardingStatus;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
