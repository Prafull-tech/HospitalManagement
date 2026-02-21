package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.SampleType;
import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestStatus;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for TestOrder.
 */
public class TestOrderResponseDto {

    private Long id;
    private String orderNumber;
    private Long patientId;
    private String patientUhid;
    private String patientName;
    private Long testMasterId;
    private String testCode;
    private String testName;
    private SampleType sampleType;
    private Long doctorId;
    private String doctorName;
    private Long ipdAdmissionId;
    private String ipdAdmissionNumber;
    private Long opdVisitId;
    private String opdVisitNumber;
    private TestStatus status;
    private LocalDateTime orderedAt;
    private LocalDateTime sampleCollectedAt;
    private String collectedBy;
    private String wardName;
    private String bedNumber;
    private LocalDateTime resultEnteredAt;
    private String resultEnteredBy;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime releasedAt;
    private String releasedBy;
    private Instant tatStartTime;
    private Instant tatEndTime;
    private TATStatus tatStatus;
    private String tatBreachReason;
    private String rejectionReason;
    private String cancellationReason;
    private String clinicalNotes;
    private Boolean isPriority;
    private Boolean billingChargePosted;
    private Long billingChargeId;

    public TestOrderResponseDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getTestMasterId() {
        return testMasterId;
    }

    public void setTestMasterId(Long testMasterId) {
        this.testMasterId = testMasterId;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getIpdAdmissionNumber() {
        return ipdAdmissionNumber;
    }

    public void setIpdAdmissionNumber(String ipdAdmissionNumber) {
        this.ipdAdmissionNumber = ipdAdmissionNumber;
    }

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public String getOpdVisitNumber() {
        return opdVisitNumber;
    }

    public void setOpdVisitNumber(String opdVisitNumber) {
        this.opdVisitNumber = opdVisitNumber;
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
