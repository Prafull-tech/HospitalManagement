package com.hospital.hms.token.dto;

import com.hospital.hms.token.entity.TokenPriority;
import com.hospital.hms.token.entity.TokenStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Token response DTO.
 */
public class TokenResponseDto {

    private Long id;
    private String tokenNo;
    private Long patientId;
    private String patientName;
    private String uhid;
    private Long doctorId;
    private String doctorName;
    private String doctorCode;
    private Long departmentId;
    private String departmentName;
    private LocalDate tokenDate;
    private Long appointmentId;
    private TokenPriority priority;
    private TokenStatus status;
    private Instant createdAt;
    private Instant calledAt;
    private Instant completedAt;
    private Long opdVisitId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTokenNo() { return tokenNo; }
    public void setTokenNo(String tokenNo) { this.tokenNo = tokenNo; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
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
    public LocalDate getTokenDate() { return tokenDate; }
    public void setTokenDate(LocalDate tokenDate) { this.tokenDate = tokenDate; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }
    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCalledAt() { return calledAt; }
    public void setCalledAt(Instant calledAt) { this.calledAt = calledAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
}
