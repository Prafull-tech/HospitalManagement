package com.hospital.hms.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequestDto {

    private LocalDate appointmentDate;
    private LocalTime slotTime;
    private Long doctorId;

    public RescheduleRequestDto() {}

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
