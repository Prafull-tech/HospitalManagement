package com.hospital.hms.hospital.dto;

import jakarta.validation.constraints.Size;

public class CustomDomainCertificateRequestDto {

    @Size(max = 30, message = "Certificate status must not exceed 30 characters")
    private String status;

    @Size(max = 1000, message = "Certificate error must not exceed 1000 characters")
    private String errorMessage;

    private String issuedAt;
    private String expiresAt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}