package com.hospital.hms.token.entity;

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

import java.time.Instant;
import java.time.LocalDate;

/**
 * OPD Token for queue management. Generated from appointment, walk-in, or emergency.
 * Flow: WAITING → CALLED → IN_CONSULTATION → COMPLETED | SKIPPED
 */
@Entity
@Table(
    name = "opd_queue_tokens",
    indexes = {
        @Index(name = "idx_token_doctor_date", columnList = "doctor_id, token_date"),
        @Index(name = "idx_token_status", columnList = "status"),
        @Index(name = "idx_token_patient", columnList = "patient_id")
    }
)
public class Token extends BaseIdEntity {

    @NotBlank
    @Size(max = 20)
    @Column(name = "token_no", nullable = false, length = 20)
    private String tokenNo;

    @NotNull
    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

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
    @Column(name = "token_date", nullable = false)
    private LocalDate tokenDate;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TokenPriority priority = TokenPriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TokenStatus status = TokenStatus.WAITING;

    @Column(name = "called_at")
    private Instant calledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "opd_visit_id")
    private Long opdVisitId;

    /** Skipped tokens move to end; higher = later in queue. */
    @Column(name = "skip_sequence")
    private Integer skipSequence;

    public Token() {
    }

    public String getTokenNo() { return tokenNo; }
    public void setTokenNo(String tokenNo) { this.tokenNo = tokenNo; }
    public Integer getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(Integer tokenNumber) { this.tokenNumber = tokenNumber; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public MedicalDepartment getDepartment() { return department; }
    public void setDepartment(MedicalDepartment department) { this.department = department; }
    public LocalDate getTokenDate() { return tokenDate; }
    public void setTokenDate(LocalDate tokenDate) { this.tokenDate = tokenDate; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }
    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }
    public Instant getCalledAt() { return calledAt; }
    public void setCalledAt(Instant calledAt) { this.calledAt = calledAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
    public Integer getSkipSequence() { return skipSequence; }
    public void setSkipSequence(Integer skipSequence) { this.skipSequence = skipSequence; }
}
