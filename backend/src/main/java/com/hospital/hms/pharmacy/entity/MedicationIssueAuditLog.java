package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for medication issue (NABH compliance).
 */
@Entity
@Table(
    name = "medication_issue_audit_log",
    indexes = {
        @Index(name = "idx_med_issue_audit_order", columnList = "medication_order_id"),
        @Index(name = "idx_med_issue_audit_at", columnList = "issued_at")
    }
)
public class MedicationIssueAuditLog extends BaseIdEntity {

    @Column(name = "medication_order_id", nullable = false)
    private Long medicationOrderId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "issued_by", nullable = false, length = 255)
    private String issuedBy;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    public MedicationIssueAuditLog() {
    }

    public Long getMedicationOrderId() {
        return medicationOrderId;
    }

    public void setMedicationOrderId(Long medicationOrderId) {
        this.medicationOrderId = medicationOrderId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
