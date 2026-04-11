package com.hospital.hms.enquiry.dto;

import com.hospital.hms.enquiry.entity.EnquiryAuditEventType;

import java.time.Instant;

public class EnquiryAuditLogDto {
    private Long id;
    private EnquiryAuditEventType eventType;
    private String performedBy;
    private Instant eventAt;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnquiryAuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(EnquiryAuditEventType eventType) {
        this.eventType = eventType;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
