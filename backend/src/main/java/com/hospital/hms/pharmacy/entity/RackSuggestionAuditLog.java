package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for rack suggestion (NABH / medication safety).
 * Logs suggested rack, final rack used, override by user.
 */
@Entity
@Table(name = "rack_suggestion_audit_log")
public class RackSuggestionAuditLog extends BaseIdEntity {

    @Column(name = "suggested_rack_id")
    private Long suggestedRackId;

    @Column(name = "suggested_rack_code", length = 20)
    private String suggestedRackCode;

    @Column(name = "final_rack_id")
    private Long finalRackId;

    @Column(name = "final_rack_code", length = 20)
    private String finalRackCode;

    @Column(name = "user_override")
    private Boolean userOverride = false;

    @Column(name = "medicine_category", length = 30)
    private String medicineCategory;

    @Column(name = "storage_type", length = 20)
    private String storageType;

    @Column(name = "lasa_flag")
    private Boolean lasaFlag;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    public Long getSuggestedRackId() {
        return suggestedRackId;
    }

    public void setSuggestedRackId(Long suggestedRackId) {
        this.suggestedRackId = suggestedRackId;
    }

    public String getSuggestedRackCode() {
        return suggestedRackCode;
    }

    public void setSuggestedRackCode(String suggestedRackCode) {
        this.suggestedRackCode = suggestedRackCode;
    }

    public Long getFinalRackId() {
        return finalRackId;
    }

    public void setFinalRackId(Long finalRackId) {
        this.finalRackId = finalRackId;
    }

    public String getFinalRackCode() {
        return finalRackCode;
    }

    public void setFinalRackCode(String finalRackCode) {
        this.finalRackCode = finalRackCode;
    }

    public Boolean getUserOverride() {
        return userOverride;
    }

    public void setUserOverride(Boolean userOverride) {
        this.userOverride = userOverride;
    }

    public String getMedicineCategory() {
        return medicineCategory;
    }

    public void setMedicineCategory(String medicineCategory) {
        this.medicineCategory = medicineCategory;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Boolean getLasaFlag() {
        return lasaFlag;
    }

    public void setLasaFlag(Boolean lasaFlag) {
        this.lasaFlag = lasaFlag;
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
