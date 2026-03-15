package com.hospital.hms.token.dto;

import com.hospital.hms.token.entity.TokenPriority;
import jakarta.validation.constraints.NotNull;

/**
 * Request to generate a token.
 */
public class TokenGenerateRequestDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private TokenPriority priority = TokenPriority.NORMAL;

    private Long appointmentId;

    public TokenGenerateRequestDto() {
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}
