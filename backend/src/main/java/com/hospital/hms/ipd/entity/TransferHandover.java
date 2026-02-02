package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Clinical handover record for patient transfers.
 * <p>
 * Records required at handover:
 * <ul>
 *   <li>Transfer Form</li>
 *   <li>Consent Form</li>
 *   <li>Handover Sheet</li>
 * </ul>
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "transfer_handover",
    indexes = {
        @Index(name = "idx_transfer_handover_transfer", columnList = "patient_transfer_id"),
        @Index(name = "idx_transfer_handover_time", columnList = "handover_time")
    }
)
public class TransferHandover extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_transfer_id", nullable = false)
    private PatientTransfer patientTransfer;

    /** Staff ID handing over (e.g. nurse/attendant from source ward). Nullable until recorded. */
    @Column(name = "handover_from_staff_id")
    private Long handoverFromStaffId;

    /** Staff ID receiving handover (e.g. nurse/attendant at destination ward). Nullable until recorded. */
    @Column(name = "handover_to_staff_id")
    private Long handoverToStaffId;

    @Size(max = 4000)
    @Column(name = "clinical_summary", length = 4000)
    private String clinicalSummary;

    /** Documents transferred (e.g. "Transfer Form, Consent Form, Handover Sheet"). Comma-separated or checklist. */
    @Size(max = 500)
    @Column(name = "documents_transferred", length = 500)
    private String documentsTransferred;

    @Column(name = "handover_time")
    private Instant handoverTime;

    public TransferHandover() {
    }

    public PatientTransfer getPatientTransfer() {
        return patientTransfer;
    }

    public void setPatientTransfer(PatientTransfer patientTransfer) {
        this.patientTransfer = patientTransfer;
    }

    public Long getHandoverFromStaffId() {
        return handoverFromStaffId;
    }

    public void setHandoverFromStaffId(Long handoverFromStaffId) {
        this.handoverFromStaffId = handoverFromStaffId;
    }

    public Long getHandoverToStaffId() {
        return handoverToStaffId;
    }

    public void setHandoverToStaffId(Long handoverToStaffId) {
        this.handoverToStaffId = handoverToStaffId;
    }

    public String getClinicalSummary() {
        return clinicalSummary;
    }

    public void setClinicalSummary(String clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }

    public String getDocumentsTransferred() {
        return documentsTransferred;
    }

    public void setDocumentsTransferred(String documentsTransferred) {
        this.documentsTransferred = documentsTransferred;
    }

    public Instant getHandoverTime() {
        return handoverTime;
    }

    public void setHandoverTime(Instant handoverTime) {
        this.handoverTime = handoverTime;
    }
}
