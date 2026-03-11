package com.hospital.hms.lab.entity;

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
 * Audit log for lab events (Section 14).
 * Tracks: Sample collected, Result entered, Result verified, Report printed.
 */
@Entity
@Table(
    name = "lab_audit_log",
    indexes = {
        @Index(name = "idx_lab_audit_test_order", columnList = "test_order_id"),
        @Index(name = "idx_lab_audit_event_at", columnList = "event_at"),
        @Index(name = "idx_lab_audit_event_type", columnList = "event_type")
    }
)
public class LabAuditLog extends BaseIdEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private LabAuditEventType eventType;

    @Column(name = "test_order_id")
    private Long testOrderId;

    @Column(name = "lab_order_item_id")
    private Long labOrderItemId;

    @Column(name = "lab_report_id")
    private Long labReportId;

    @Size(max = 50)
    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Size(max = 255)
    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @NotNull
    @Column(name = "event_at", nullable = false)
    private Instant eventAt;

    @Size(max = 500)
    @Column(name = "details", length = 500)
    private String details;

    @Size(max = 100)
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    public LabAuditLog() {
    }

    public LabAuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(LabAuditEventType eventType) {
        this.eventType = eventType;
    }

    public Long getTestOrderId() {
        return testOrderId;
    }

    public void setTestOrderId(Long testOrderId) {
        this.testOrderId = testOrderId;
    }

    public Long getLabOrderItemId() {
        return labOrderItemId;
    }

    public void setLabOrderItemId(Long labOrderItemId) {
        this.labOrderItemId = labOrderItemId;
    }

    public Long getLabReportId() {
        return labReportId;
    }

    public void setLabReportId(Long labReportId) {
        this.labReportId = labReportId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getEventAt() {
        return eventAt;
    }

    public void setEventAt(Instant eventAt) {
        this.eventAt = eventAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
