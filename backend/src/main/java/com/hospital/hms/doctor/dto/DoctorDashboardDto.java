package com.hospital.hms.doctor.dto;

import com.hospital.hms.appointment.dto.AppointmentResponseDto;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;

import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardDto {

    private long todayAppointments;
    private long waitingPatients;
    private long completedConsultations;
    private long pendingLabReports;
    private List<OPDVisitResponseDto> recentPatients = new ArrayList<>();
    private List<OPDVisitResponseDto> todayQueue = new ArrayList<>();
    private List<AppointmentResponseDto> upcomingAppointments = new ArrayList<>();

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getWaitingPatients() {
        return waitingPatients;
    }

    public void setWaitingPatients(long waitingPatients) {
        this.waitingPatients = waitingPatients;
    }

    public long getCompletedConsultations() {
        return completedConsultations;
    }

    public void setCompletedConsultations(long completedConsultations) {
        this.completedConsultations = completedConsultations;
    }

    public long getPendingLabReports() {
        return pendingLabReports;
    }

    public void setPendingLabReports(long pendingLabReports) {
        this.pendingLabReports = pendingLabReports;
    }

    public List<OPDVisitResponseDto> getRecentPatients() {
        return recentPatients;
    }

    public void setRecentPatients(List<OPDVisitResponseDto> recentPatients) {
        this.recentPatients = recentPatients != null ? recentPatients : new ArrayList<>();
    }

    public List<OPDVisitResponseDto> getTodayQueue() {
        return todayQueue;
    }

    public void setTodayQueue(List<OPDVisitResponseDto> todayQueue) {
        this.todayQueue = todayQueue != null ? todayQueue : new ArrayList<>();
    }

    public List<AppointmentResponseDto> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(List<AppointmentResponseDto> upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments != null ? upcomingAppointments : new ArrayList<>();
    }
}