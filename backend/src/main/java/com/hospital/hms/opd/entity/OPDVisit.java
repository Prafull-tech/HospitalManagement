package com.hospital.hms.opd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.MedicalDepartment;
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

import java.time.LocalDate;

/**
 * OPD visit. Links patient (Reception) and doctor (Doctors module). DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "opd_visits",
    indexes = {
        @Index(name = "idx_opd_visit_date", columnList = "visit_date"),
        @Index(name = "idx_opd_visit_doctor", columnList = "doctor_id"),
        @Index(name = "idx_opd_visit_status", columnList = "visit_status"),
        @Index(name = "idx_opd_visit_number", columnList = "visit_number", unique = true)
    }
)
public class OPDVisit extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "visit_number", nullable = false, unique = true, length = 50)
    private String visitNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private MedicalDepartment department;

    @NotNull
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_status", nullable = false, length = 30)
    private VisitStatus visitStatus = VisitStatus.REGISTERED;

    @Column(name = "token_number")
    private Integer tokenNumber;

    @Column(name = "referred_to_department_id")
    private Long referredToDepartmentId;

    @Column(name = "referred_to_doctor_id")
    private Long referredToDoctorId;

    @Column(name = "refer_to_ipd")
    private Boolean referToIpd = false;

    @Size(max = 500)
    @Column(name = "referral_remarks", length = 500)
    private String referralRemarks;

    public OPDVisit() {
    }

    public String getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public MedicalDepartment getDepartment() {
        return department;
    }

    public void setDepartment(MedicalDepartment department) {
        this.department = department;
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
}
