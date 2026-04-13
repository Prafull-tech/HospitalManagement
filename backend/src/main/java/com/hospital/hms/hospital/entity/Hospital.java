package com.hospital.hms.hospital.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Hospital / branch master. Multi-hospital ready. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "hospitals",
    indexes = {
        @Index(name = "idx_hospital_code", columnList = "hospital_code", unique = true),
        @Index(name = "idx_hospital_active", columnList = "is_active")
    }
)
public class Hospital extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "hospital_code", nullable = false, unique = true, length = 50)
    private String hospitalCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "hospital_name", nullable = false, length = 255)
    private String hospitalName;

    @Size(max = 500)
    @Column(name = "location", length = 500)
    private String location;

    @Size(max = 100)
    @Column(name = "subdomain", unique = true, length = 100)
    private String subdomain;

    @Size(max = 255)
    @Column(name = "custom_domain", unique = true, length = 255)
    private String customDomain;

    @Size(max = 120)
    @Column(name = "domain_verification_token", length = 120)
    private String domainVerificationToken;

    @Size(max = 30)
    @Column(name = "domain_verification_status", length = 30)
    private String domainVerificationStatus = "NOT_CONFIGURED";

    @Column(name = "domain_verified_at")
    private java.time.Instant domainVerifiedAt;

    @Size(max = 30)
    @Column(name = "certificate_status", length = 30)
    private String certificateStatus = "NOT_REQUESTED";

    @Column(name = "certificate_requested_at")
    private java.time.Instant certificateRequestedAt;

    @Column(name = "certificate_issued_at")
    private java.time.Instant certificateIssuedAt;

    @Column(name = "certificate_expires_at")
    private java.time.Instant certificateExpiresAt;

    @Size(max = 1000)
    @Column(name = "last_domain_verification_error", length = 1000)
    private String lastDomainVerificationError;

    @Size(max = 1000)
    @Column(name = "last_certificate_error", length = 1000)
    private String lastCertificateError;

    @Size(max = 255)
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Size(max = 255)
    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Size(max = 255)
    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Size(max = 255)
    @Column(name = "twitter_url", length = 255)
    private String twitterUrl;

    @Size(max = 255)
    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Size(max = 255)
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Size(max = 100)
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Size(max = 100)
    @Column(name = "billing_email", length = 100)
    private String billingEmail;

    @Size(max = 20)
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Size(max = 30)
    @Column(name = "onboarding_status", length = 30)
    private String onboardingStatus = "PENDING";

    public Hospital() {
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

    public java.time.Instant getDomainVerifiedAt() {
        return domainVerifiedAt;
    }

    public void setDomainVerifiedAt(java.time.Instant domainVerifiedAt) {
        this.domainVerifiedAt = domainVerifiedAt;
    }

    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(String certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public java.time.Instant getCertificateRequestedAt() {
        return certificateRequestedAt;
    }

    public void setCertificateRequestedAt(java.time.Instant certificateRequestedAt) {
        this.certificateRequestedAt = certificateRequestedAt;
    }

    public java.time.Instant getCertificateIssuedAt() {
        return certificateIssuedAt;
    }

    public void setCertificateIssuedAt(java.time.Instant certificateIssuedAt) {
        this.certificateIssuedAt = certificateIssuedAt;
    }

    public java.time.Instant getCertificateExpiresAt() {
        return certificateExpiresAt;
    }

    public void setCertificateExpiresAt(java.time.Instant certificateExpiresAt) {
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getBillingEmail() { return billingEmail; }
    public void setBillingEmail(String billingEmail) { this.billingEmail = billingEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(String onboardingStatus) { this.onboardingStatus = onboardingStatus; }
}
