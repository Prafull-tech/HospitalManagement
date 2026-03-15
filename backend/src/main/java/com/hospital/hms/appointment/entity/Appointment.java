package com.hospital.hms.appointment.entity;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment entity. Links patient, doctor, department. Supports walk-in, online, front-desk booking.
 */
@Entity
@Table(
    name = "appointments",
    indexes = {
        @Index(name = "idx_appointment_date", columnList = "appointment_date"),
        @Index(name = "idx_appointment_doctor", columnList = "doctor_id"),
        @Index(name = "idx_appointment_status", columnList = "status"),
        @Index(name = "idx_appointment_patient", columnList = "patient_id")
    }
)
public class Appointment extends BaseIdEntity {

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
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull
    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "token_no")
    private Integer tokenNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private AppointmentSource source = AppointmentSource.FRONT_DESK;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", length = 20)
    private AppointmentVisitType visitType;

    @Size(max = 255)
    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Size(max = 500)
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "opd_visit_id")
    private Long opdVisitId;

    public Appointment() {
    }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public MedicalDepartment getDepartment() { return department; }
    public void setDepartment(MedicalDepartment department) { this.department = department; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
    public Integer getTokenNo() { return tokenNo; }
    public void setTokenNo(Integer tokenNo) { this.tokenNo = tokenNo; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public AppointmentSource getSource() { return source; }
    public void setSource(AppointmentSource source) { this.source = source; }
    public AppointmentVisitType getVisitType() { return visitType; }
    public void setVisitType(AppointmentVisitType visitType) { this.visitType = visitType; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
}
