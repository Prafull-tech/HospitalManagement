package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.reception.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Laboratory test order. Can be for OPD or IPD patients.
 * For IPD: linked to IPD Admission Number.
 * For OPD: linked to OPD Visit.
 * All orders must have doctor reference.
 */
@Entity
@Table(
    name = "lab_test_orders",
    indexes = {
        @Index(name = "idx_test_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_test_order_patient", columnList = "patient_id"),
        @Index(name = "idx_test_order_ipd", columnList = "ipd_admission_id"),
        @Index(name = "idx_test_order_opd", columnList = "opd_visit_id"),
        @Index(name = "idx_test_order_status", columnList = "status"),
        @Index(name = "idx_test_order_ordered_at", columnList = "ordered_at"),
        @Index(name = "idx_test_order_test_master", columnList = "test_master_id")
    }
)
public class TestOrder extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_master_id", nullable = false)
    private TestMaster testMaster;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id")
    private IPDAdmission ipdAdmission; // Null for OPD orders

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_visit_id")
    private OPDVisit opdVisit; // Null for IPD orders

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TestStatus status = TestStatus.ORDERED;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @Size(max = 255)
    @Column(name = "collected_by", length = 255)
    private String collectedBy; // Phlebotomist username

    @Size(max = 255)
    @Column(name = "ward_name", length = 255)
    private String wardName; // For IPD: ward where sample was collected

    @Size(max = 50)
    @Column(name = "bed_number", length = 50)
    private String bedNumber; // For IPD: bed number

    @Column(name = "result_entered_at")
    private LocalDateTime resultEnteredAt;

    @Size(max = 255)
    @Column(name = "result_entered_by", length = 255)
    private String resultEnteredBy; // Lab technician username

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Size(max = 255)
    @Column(name = "verified_by", length = 255)
    private String verifiedBy; // Lab supervisor username

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Size(max = 255)
    @Column(name = "released_by", length = 255)
    private String releasedBy;

    @Column(name = "tat_start_time")
    private Instant tatStartTime; // TAT timer starts at sample collection

    @Column(name = "tat_end_time")
    private Instant tatEndTime; // TAT timer ends at report release

    @Enumerated(EnumType.STRING)
    @Column(name = "tat_status", length = 20)
    private TATStatus tatStatus;

    @Size(max = 500)
    @Column(name = "tat_breach_reason", length = 500)
    private String tatBreachReason; // If TAT breached, reason captured

    @Size(max = 500)
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason; // If sample rejected

    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason; // If test cancelled

    @Size(max = 1000)
    @Column(name = "clinical_notes", length = 1000)
    private String clinicalNotes; // Doctor's clinical notes

    @NotNull
    @Column(name = "is_priority", nullable = false)
    private Boolean isPriority = false; // Emergency / ICU tests

    @Column(name = "billing_charge_posted", nullable = false)
    private Boolean billingChargePosted = false;

    @Column(name = "billing_charge_id")
    private Long billingChargeId; // Reference to AdmissionCharge if IPD

    public TestOrder() {
    }

    // Getters and setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public TestMaster getTestMaster() {
        return testMaster;
    }

    public void setTestMaster(TestMaster testMaster) {
        this.testMaster = testMaster;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public OPDVisit getOpdVisit() {
        return opdVisit;
    }

    public void setOpdVisit(OPDVisit opdVisit) {
        this.opdVisit = opdVisit;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public LocalDateTime getSampleCollectedAt() {
        return sampleCollectedAt;
    }

    public void setSampleCollectedAt(LocalDateTime sampleCollectedAt) {
        this.sampleCollectedAt = sampleCollectedAt;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public LocalDateTime getResultEnteredAt() {
        return resultEnteredAt;
    }

    public void setResultEnteredAt(LocalDateTime resultEnteredAt) {
        this.resultEnteredAt = resultEnteredAt;
    }

    public String getResultEnteredBy() {
        return resultEnteredBy;
    }

    public void setResultEnteredBy(String resultEnteredBy) {
        this.resultEnteredBy = resultEnteredBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getReleasedBy() {
        return releasedBy;
    }

    public void setReleasedBy(String releasedBy) {
        this.releasedBy = releasedBy;
    }

    public Instant getTatStartTime() {
        return tatStartTime;
    }

    public void setTatStartTime(Instant tatStartTime) {
        this.tatStartTime = tatStartTime;
    }

    public Instant getTatEndTime() {
        return tatEndTime;
    }

    public void setTatEndTime(Instant tatEndTime) {
        this.tatEndTime = tatEndTime;
    }

    public TATStatus getTatStatus() {
        return tatStatus;
    }

    public void setTatStatus(TATStatus tatStatus) {
        this.tatStatus = tatStatus;
    }

    public String getTatBreachReason() {
        return tatBreachReason;
    }

    public void setTatBreachReason(String tatBreachReason) {
        this.tatBreachReason = tatBreachReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public Boolean getIsPriority() {
        return isPriority;
    }

    public void setIsPriority(Boolean isPriority) {
        this.isPriority = isPriority;
    }

    public Boolean getBillingChargePosted() {
        return billingChargePosted;
    }

    public void setBillingChargePosted(Boolean billingChargePosted) {
        this.billingChargePosted = billingChargePosted;
    }

    public Long getBillingChargeId() {
        return billingChargeId;
    }

    public void setBillingChargeId(Long billingChargeId) {
        this.billingChargeId = billingChargeId;
    }
}
