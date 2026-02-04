package com.hospital.hms.ipd.service;

import com.hospital.hms.billing.entity.AdmissionCharge;
import com.hospital.hms.billing.service.AdmissionChargeService;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.reception.dto.PatientResponseDto;
import com.hospital.hms.reception.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Read-only view service for IPD admission detail page.
 * Aggregates admission, patient, timeline, and billing summary.
 */
@Service
public class IPDAdmissionViewService {

    private final IPDAdmissionService admissionService;
    private final PatientService patientService;
    private final IPDAdmissionTimelineService timelineService;
    private final AdmissionChargeService admissionChargeService;

    public IPDAdmissionViewService(IPDAdmissionService admissionService,
                                   PatientService patientService,
                                   IPDAdmissionTimelineService timelineService,
                                   AdmissionChargeService admissionChargeService) {
        this.admissionService = admissionService;
        this.patientService = patientService;
        this.timelineService = timelineService;
        this.admissionChargeService = admissionChargeService;
    }

    /**
     * Full view for GET /api/ipd/admissions/{id}/view.
     * Returns 404 if admission not found.
     */
    @Transactional(readOnly = true)
    public ViewAdmissionResponseDto getViewById(Long admissionId) {
        IPDAdmissionResponseDto admission = admissionService.getById(admissionId);
        PatientResponseDto patient = patientService.getById(admission.getPatientId());
        List<TimelineEventDto> timeline = timelineService.getTimeline(admissionId);
        BillingSummaryDto billingSummary = buildBillingSummary(admissionId, admission);

        ViewAdmissionResponseDto view = new ViewAdmissionResponseDto();
        view.setAdmission(admission);
        view.setPatient(patient);
        view.setTimeline(timeline);
        view.setBillingSummary(billingSummary);
        return view;
    }

    private BillingSummaryDto buildBillingSummary(Long admissionId, IPDAdmissionResponseDto admission) {
        BillingSummaryDto summary = new BillingSummaryDto();
        BigDecimal deposit = admission.getDepositAmount() != null ? admission.getDepositAmount() : BigDecimal.ZERO;
        summary.setTotalDeposit(deposit);

        List<AdmissionCharge> charges = admissionChargeService.findByIpdAdmissionId(admissionId);
        BigDecimal total = charges.stream()
                .map(AdmissionCharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalCharges(total);
        summary.setChargeCount(charges.size());
        summary.setBillingStatus(total.compareTo(deposit) > 0 ? "Pending" : "Cleared");
        return summary;
    }
}
