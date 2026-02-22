package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment transaction record for audit trail.
 * NABH / medico-legal compliant.
 */
@Entity
@Table(
    name = "billing_payments",
    indexes = {
        @Index(name = "idx_payment_ipd", columnList = "ipd_admission_id"),
        @Index(name = "idx_payment_created", columnList = "created_at")
    }
)
public class Payment extends BaseIdEntity {

    @NotNull
    @Column(name = "ipd_admission_id", nullable = false)
    private Long ipdAdmissionId;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Size(max = 30)
    @Column(name = "mode", nullable = false, length = 30)
    private String mode;

    @Size(max = 100)
    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Size(max = 255)
    @Column(name = "created_by", length = 255)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Size(max = 100)
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    public Payment() {
    }

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
