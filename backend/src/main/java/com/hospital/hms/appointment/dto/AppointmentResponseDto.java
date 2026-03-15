package com.hospital.hms.appointment.dto;

import com.hospital.hms.appointment.entity.AppointmentSource;
import com.hospital.hms.appointment.entity.AppointmentStatus;
import com.hospital.hms.appointment.entity.AppointmentVisitType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponseDto {

    private Long id;
    private Long patientId;
    private String patientUhid;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorCode;
    private Long departmentId;
    private String departmentName;
    private LocalDate appointmentDate;
    private LocalTime slotTime;
    private Integer tokenNo;
    private AppointmentStatus status;
    private AppointmentSource source;
    private AppointmentVisitType visitType;
    private String createdBy;
    private String cancelReason;
    private Long opdVisitId;
    private Instant createdAt;
    private Instant updatedAt;

    public AppointmentResponseDto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorCode() { return doctorCode; }
    public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
