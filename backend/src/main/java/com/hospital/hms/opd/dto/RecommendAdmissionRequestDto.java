package com.hospital.hms.opd.dto;

import com.hospital.hms.opd.entity.ConsultationOutcome;

/**
 * Optional request body for PUT /api/visit/{id}/recommend-admission.
 * Doctor explicitly marks "Admission Recommended"; optionally sets consultation outcome to IPD_ADMISSION_ADVISED.
 */
public class RecommendAdmissionRequestDto {

    /** Optional: set consultation outcome to IPD_ADMISSION_ADVISED when recommending admission. */
    private ConsultationOutcome consultationOutcome;

    public RecommendAdmissionRequestDto() {
    }

    public ConsultationOutcome getConsultationOutcome() {
        return consultationOutcome;
    }

    public void setConsultationOutcome(ConsultationOutcome consultationOutcome) {
        this.consultationOutcome = consultationOutcome;
    }
}
