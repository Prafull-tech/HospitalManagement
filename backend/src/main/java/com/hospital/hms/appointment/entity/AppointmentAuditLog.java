package com.hospital.hms.appointment.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Audit log for appointment events: created, rescheduled, cancelled, no_show.
 */
@Entity
@Table(
    name = "appointment_audit_log",
    indexes = {
        @Index(name = "idx_appt_audit_appointment", columnList = "appointment_id"),
        @Index(name = "idx_appt_audit_created", columnList = "created_at")
    }
)
public class AppointmentAuditLog extends BaseIdEntity {

    @NotNull
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private AppointmentAuditEventType eventType;

    @Size(max = 255)
    @Column(name = "user_id", length = 255)
    private String userId;

    @Size(max = 100)
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "event_at", nullable = false)
    private Instant eventAt = Instant.now();

    public AppointmentAuditLog() {
    }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public AppointmentAuditEventType getEventType() { return eventType; }
    public void setEventType(AppointmentAuditEventType eventType) { this.eventType = eventType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Instant getEventAt() { return eventAt; }
    public void setEventAt(Instant eventAt) { this.eventAt = eventAt; }
}
