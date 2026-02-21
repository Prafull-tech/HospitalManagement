package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for medicine entry operations (NABH / LASA compliance).
 * Logs entry mode, barcode (if used), Excel filename (if import), user, timestamp, correlation ID.
 */
@Entity
@Table(name = "medicine_entry_audit_log")
public class MedicineEntryAuditLog extends BaseIdEntity {

    @Column(name = "entry_mode", nullable = false, length = 30)
    private String entryMode; // MANUAL, BARCODE, EXISTING, IMPORT

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "excel_filename", length = 255)
    private String excelFilename;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getExcelFilename() {
        return excelFilename;
    }

    public void setExcelFilename(String excelFilename) {
        this.excelFilename = excelFilename;
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
