package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for medicine barcode lookup (NABH / medication safety).
 * Logs barcode scanned, external lookup used, saved locally, userId, correlationId.
 */
@Entity
@Table(name = "medicine_lookup_audit_log")
public class MedicineLookupAuditLog extends BaseIdEntity {

    @Column(name = "barcode", nullable = false, length = 50)
    private String barcode;

    @Column(name = "lookup_source", length = 20)
    private String lookupSource; // LOCAL, EXTERNAL

    @Column(name = "external_lookup_used")
    private Boolean externalLookupUsed = false;

    @Column(name = "saved_locally")
    private Boolean savedLocally = false;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLookupSource() {
        return lookupSource;
    }

    public void setLookupSource(String lookupSource) {
        this.lookupSource = lookupSource;
    }

    public Boolean getExternalLookupUsed() {
        return externalLookupUsed;
    }

    public void setExternalLookupUsed(Boolean externalLookupUsed) {
        this.externalLookupUsed = externalLookupUsed;
    }

    public Boolean getSavedLocally() {
        return savedLocally;
    }

    public void setSavedLocally(Boolean savedLocally) {
        this.savedLocally = savedLocally;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
