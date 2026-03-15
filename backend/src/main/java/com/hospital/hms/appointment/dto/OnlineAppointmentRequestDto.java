package com.hospital.hms.appointment.dto;

import com.hospital.hms.appointment.entity.AppointmentVisitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class OnlineAppointmentRequestDto {

    @NotNull
    @Size(max = 50)
    private String patientUhid;

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime slotTime;

    private AppointmentVisitType visitType;

    public OnlineAppointmentRequestDto() {}

    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
    public AppointmentVisitType getVisitType() { return visitType; }
    public void setVisitType(AppointmentVisitType visitType) { this.visitType = visitType; }
}
