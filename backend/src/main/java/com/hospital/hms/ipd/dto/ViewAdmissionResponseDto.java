package com.hospital.hms.ipd.dto;

import com.hospital.hms.reception.dto.PatientResponseDto;

import java.util.List;

/**
 * Full read-only view of an IPD admission for the View Admission page.
 * Includes admission details, patient, timeline, and billing summary.
 */
public class ViewAdmissionResponseDto {

    private IPDAdmissionResponseDto admission;
    private PatientResponseDto patient;
    private List<TimelineEventDto> timeline;
    private BillingSummaryDto billingSummary;

    public ViewAdmissionResponseDto() {
    }

    public IPDAdmissionResponseDto getAdmission() {
        return admission;
    }

    public void setAdmission(IPDAdmissionResponseDto admission) {
        this.admission = admission;
    }

    public PatientResponseDto getPatient() {
        return patient;
    }

    public void setPatient(PatientResponseDto patient) {
        this.patient = patient;
    }

    public List<TimelineEventDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEventDto> timeline) {
        this.timeline = timeline;
    }

    public BillingSummaryDto getBillingSummary() {
        return billingSummary;
    }

    public void setBillingSummary(BillingSummaryDto billingSummary) {
        this.billingSummary = billingSummary;
    }
}
