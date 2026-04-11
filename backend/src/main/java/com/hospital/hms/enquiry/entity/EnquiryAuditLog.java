package com.hospital.hms.enquiry.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
    name = "enquiry_audit_logs",
    indexes = {
        @Index(name = "idx_enquiry_audit_enquiry", columnList = "enquiry_id"),
        @Index(name = "idx_enquiry_audit_event", columnList = "event_type")
    }
)
public class EnquiryAuditLog extends BaseIdEntity {

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EnquiryAuditEventType eventType;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "event_at", nullable = false)
    private Instant eventAt;

    @Column(name = "note", length = 1000)
    private String note;

    public Long getEnquiryId() {
        return enquiryId;
    }

    public void setEnquiryId(Long enquiryId) {
        this.enquiryId = enquiryId;
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
