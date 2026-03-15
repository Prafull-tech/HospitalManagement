package com.hospital.hms.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class WalkInAppointmentRequestDto {

    @NotNull(message = "Patient UHID is required")
    @Size(max = 50)
    private String patientUhid;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private LocalDate appointmentDate;
    private LocalTime slotTime;

    public WalkInAppointmentRequestDto() {}

    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
}
