package com.hospital.hms.opd.dto;

import com.hospital.hms.opd.entity.VisitStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for OPD visit (view/list).
 */
public class OPDVisitResponseDto {

    private Long id;
    private String visitNumber;
    private String patientUhid;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorCode;
    private Long departmentId;
    private String departmentName;
    private LocalDate visitDate;
    private VisitStatus visitStatus;
    private Integer tokenNumber;
    private Long referredToDepartmentId;
    private Long referredToDoctorId;
    private Boolean referToIpd;
    private String referralRemarks;
    private OPDClinicalNoteResponseDto clinicalNote;
    private Instant createdAt;
    private Instant updatedAt;

    public OPDVisitResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public VisitStatus getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(VisitStatus visitStatus) {
        this.visitStatus = visitStatus;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(Integer tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public Long getReferredToDepartmentId() {
        return referredToDepartmentId;
    }

    public void setReferredToDepartmentId(Long referredToDepartmentId) {
        this.referredToDepartmentId = referredToDepartmentId;
    }

    public Long getReferredToDoctorId() {
        return referredToDoctorId;
    }

    public void setReferredToDoctorId(Long referredToDoctorId) {
        this.referredToDoctorId = referredToDoctorId;
    }

    public Boolean getReferToIpd() {
        return referToIpd;
    }

    public void setReferToIpd(Boolean referToIpd) {
        this.referToIpd = referToIpd;
    }

    public String getReferralRemarks() {
        return referralRemarks;
    }

    public void setReferralRemarks(String referralRemarks) {
        this.referralRemarks = referralRemarks;
    }

    public OPDClinicalNoteResponseDto getClinicalNote() {
        return clinicalNote;
    }

    public void setClinicalNote(OPDClinicalNoteResponseDto clinicalNote) {
        this.clinicalNote = clinicalNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
