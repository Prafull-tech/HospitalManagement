package com.hospital.hms.ipd.dto;

import java.time.Instant;

/**
 * Single event in the IPD admission timeline. All activities linked with IPD Admission Number.
 */
public class TimelineEventDto {

    /** Event type: ADMISSION, NURSING_NOTE, VITAL_SIGN, MEDICATION, DOCTOR_ORDER, PHARMACY, LAB, BILLING_CHARGE. */
    private String eventType;
    private Instant timestamp;
    private String title;
    private String description;
    /** Source module for display (e.g. Nursing, Pharmacy, Lab). */
    private String sourceModule;
    /** Reference to source entity (e.g. note id, order id) for deep link. */
    private Long referenceId;

    public TimelineEventDto() {
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}
