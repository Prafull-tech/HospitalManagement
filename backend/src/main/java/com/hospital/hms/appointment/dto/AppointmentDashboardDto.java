package com.hospital.hms.appointment.dto;

import java.time.LocalDate;
import java.util.List;

public class AppointmentDashboardDto {

    private LocalDate date;
    private long totalAppointmentsToday;
    private long walkIns;
    private long onlineBookings;
    private long completedConsultations;
    private long cancelled;
    private long noShow;
    private List<AppointmentResponseDto> todaysAppointments;
    private List<AppointmentResponseDto> upcomingAppointments;
    private List<AppointmentResponseDto> cancelledAppointments;
    private List<AppointmentResponseDto> noShowPatients;

    public AppointmentDashboardDto() {
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalAppointmentsToday() { return totalAppointmentsToday; }
    public void setTotalAppointmentsToday(long totalAppointmentsToday) { this.totalAppointmentsToday = totalAppointmentsToday; }
    public long getWalkIns() { return walkIns; }
    public void setWalkIns(long walkIns) { this.walkIns = walkIns; }
    public long getOnlineBookings() { return onlineBookings; }
    public void setOnlineBookings(long onlineBookings) { this.onlineBookings = onlineBookings; }
    public long getCompletedConsultations() { return completedConsultations; }
    public void setCompletedConsultations(long completedConsultations) { this.completedConsultations = completedConsultations; }
    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    public long getNoShow() { return noShow; }
    public void setNoShow(long noShow) { this.noShow = noShow; }
    public List<AppointmentResponseDto> getTodaysAppointments() { return todaysAppointments; }
    public void setTodaysAppointments(List<AppointmentResponseDto> todaysAppointments) { this.todaysAppointments = todaysAppointments; }
    public List<AppointmentResponseDto> getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(List<AppointmentResponseDto> upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }
    public List<AppointmentResponseDto> getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(List<AppointmentResponseDto> cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
    public List<AppointmentResponseDto> getNoShowPatients() { return noShowPatients; }
    public void setNoShowPatients(List<AppointmentResponseDto> noShowPatients) { this.noShowPatients = noShowPatients; }
}
