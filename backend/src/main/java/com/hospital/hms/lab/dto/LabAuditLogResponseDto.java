package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.LabAuditEventType;

import java.time.Instant;

/**
 * Response DTO for lab audit log entries.
 */
public class LabAuditLogResponseDto {

    private Long id;
    private LabAuditEventType eventType;
    private Long testOrderId;
    private Long labOrderItemId;
    private Long labReportId;
    private String orderNumber;
    private String performedBy;
    private Instant eventAt;
    private String details;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LabAuditEventType getEventType() { return eventType; }
    public void setEventType(LabAuditEventType eventType) { this.eventType = eventType; }
    public Long getTestOrderId() { return testOrderId; }
    public void setTestOrderId(Long testOrderId) { this.testOrderId = testOrderId; }
    public Long getLabOrderItemId() { return labOrderItemId; }
    public void setLabOrderItemId(Long labOrderItemId) { this.labOrderItemId = labOrderItemId; }
    public Long getLabReportId() { return labReportId; }
    public void setLabReportId(Long labReportId) { this.labReportId = labReportId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public Instant getEventAt() { return eventAt; }
    public void setEventAt(Instant eventAt) { this.eventAt = eventAt; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
