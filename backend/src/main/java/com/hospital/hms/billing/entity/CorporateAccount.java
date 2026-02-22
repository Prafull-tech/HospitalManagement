package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Corporate account for tie-up billing. Credit limit, billing cycle.
 * NABH / Corporate compliant.
 */
@Entity
@Table(
    name = "corporate_accounts",
    indexes = {
        @Index(name = "idx_corporate_code", columnList = "corporate_code", unique = true)
    }
)
public class CorporateAccount extends BaseIdEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "corporate_code", nullable = false, unique = true, length = 50)
    private String corporateCode;

    @Size(max = 255)
    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @NotNull
    @Column(name = "credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public enum BillingCycle { MONTHLY, QUARTERLY }

    public CorporateAccount() {
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCorporateCode() { return corporateCode; }
    public void setCorporateCode(String corporateCode) { this.corporateCode = corporateCode; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
