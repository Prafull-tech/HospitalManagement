package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * EMI plan for billing account. Allows discharge when EMI active.
 * NABH / Corporate compliant.
 */
@Entity
@Table(
    name = "emi_plans",
    indexes = {
        @Index(name = "idx_emi_billing_account", columnList = "billing_account_id"),
        @Index(name = "idx_emi_status", columnList = "status")
    }
)
public class EMIPlan extends BaseIdEntity {

    @NotNull
    @Column(name = "billing_account_id", nullable = false)
    private Long billingAccountId;

    @Column(name = "ipd_admission_id")
    private Long ipdAdmissionId;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "down_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal downPayment = BigDecimal.ZERO;

    @NotNull
    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths = 1;

    @NotNull
    @Column(name = "emi_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount = BigDecimal.ZERO;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EMIPlanStatus status = EMIPlanStatus.ACTIVE;

    public enum EMIPlanStatus { ACTIVE, COMPLETED, DEFAULTED, CANCELLED }

    public EMIPlan() {
    }

    public Long getBillingAccountId() { return billingAccountId; }
    public void setBillingAccountId(Long billingAccountId) { this.billingAccountId = billingAccountId; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDownPayment() { return downPayment; }
    public void setDownPayment(BigDecimal downPayment) { this.downPayment = downPayment; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public EMIPlanStatus getStatus() { return status; }
    public void setStatus(EMIPlanStatus status) { this.status = status; }
}
