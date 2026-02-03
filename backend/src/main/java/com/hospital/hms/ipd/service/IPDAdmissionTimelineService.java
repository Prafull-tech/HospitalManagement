package com.hospital.hms.ipd.service;

import com.hospital.hms.billing.repository.AdmissionChargeRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.dto.TimelineEventDto;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.DoctorOrderRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.nursing.entity.VitalSignRecord;
import com.hospital.hms.nursing.repository.MedicationAdministrationRepository;
import com.hospital.hms.nursing.repository.NursingNoteRepository;
import com.hospital.hms.nursing.repository.VitalSignRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Timeline view per patient: aggregates all activities linked with IPD Admission Number.
 * Includes admission, nursing notes, vitals, MAR; extended by billing and (when present) doctor orders, pharmacy, lab.
 */
@Service
public class IPDAdmissionTimelineService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final IPDAdmissionRepository admissionRepository;
    private final NursingNoteRepository nursingNoteRepository;
    private final VitalSignRecordRepository vitalSignRepository;
    private final MedicationAdministrationRepository marRepository;
    private final DoctorOrderRepository doctorOrderRepository;
    private final AdmissionChargeRepository admissionChargeRepository;

    public IPDAdmissionTimelineService(IPDAdmissionRepository admissionRepository,
                                       NursingNoteRepository nursingNoteRepository,
                                       VitalSignRecordRepository vitalSignRepository,
                                       MedicationAdministrationRepository marRepository,
                                       DoctorOrderRepository doctorOrderRepository,
                                       AdmissionChargeRepository admissionChargeRepository) {
        this.admissionRepository = admissionRepository;
        this.nursingNoteRepository = nursingNoteRepository;
        this.vitalSignRepository = vitalSignRepository;
        this.marRepository = marRepository;
        this.doctorOrderRepository = doctorOrderRepository;
        this.admissionChargeRepository = admissionChargeRepository;
    }

    @Transactional(readOnly = true)
    public List<TimelineEventDto> getTimeline(Long admissionId) {
        IPDAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + admissionId));

        List<TimelineEventDto> events = new ArrayList<>();

        // Admission created
        events.add(buildEvent("ADMISSION", toInstant(admission.getAdmissionDateTime()),
                "Admission", "Admitted — " + admission.getAdmissionNumber(),
                "IPD", admission.getId()));

        // Shift to ward
        if (admission.getShiftedToWardAt() != null) {
            events.add(buildEvent("ADMISSION", admission.getShiftedToWardAt(),
                    "Shift to ward", "Patient shifted to ward",
                    "Nursing", admission.getId()));
        }

        // Nursing notes
        nursingNoteRepository.findByIpdAdmissionIdOrderByRecordedAtDesc(admissionId)
                .forEach(n -> {
                    String desc = n.getNoteType();
                    if (n.getContent() != null && !n.getContent().isBlank()) {
                        String snippet = n.getContent().length() > 80 ? n.getContent().substring(0, 80) + "…" : n.getContent();
                        desc = desc + " — " + snippet;
                    }
                    events.add(buildEvent("NURSING_NOTE", toInstant(n.getRecordedAt()),
                            "Nursing note", desc, "Nursing", n.getId()));
                });

        // Vital signs
        vitalSignRepository.findByIpdAdmissionIdOrderByRecordedAtDesc(admissionId)
                .forEach(v -> events.add(buildEvent("VITAL_SIGN", toInstant(v.getRecordedAt()),
                        "Vital signs", formatVitals(v),
                        "Nursing", v.getId())));

        // MAR
        marRepository.findByIpdAdmissionIdOrderByAdministeredAtDesc(admissionId)
                .forEach(m -> events.add(buildEvent("MEDICATION", toInstant(m.getAdministeredAt()),
                        "Medication", m.getMedicationName() + (m.getDosage() != null ? " — " + m.getDosage() : ""),
                        "Nursing", m.getId())));

        // Doctor orders (linked to IPD admission)
        doctorOrderRepository.findByIpdAdmissionIdOrderByOrderedAtDesc(admissionId)
                .forEach(o -> {
                    String desc = o.getOrderType().name();
                    if (o.getDescription() != null && !o.getDescription().isBlank()) {
                        String snippet = o.getDescription().length() > 80 ? o.getDescription().substring(0, 80) + "…" : o.getDescription();
                        desc = desc + " — " + snippet;
                    }
                    desc = desc + " (" + o.getStatus() + ")";
                    events.add(buildEvent("DOCTOR_ORDER", toInstant(o.getOrderedAt()),
                            "Doctor order", desc, "Doctor Orders", o.getId()));
                });

        // Billing charges (auto-added by Pharmacy, Lab, Doctor Orders, etc.)
        admissionChargeRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(admissionId)
                .forEach(c -> events.add(buildEvent("BILLING_CHARGE", c.getCreatedAt(),
                        "Charge", (c.getDescription() != null ? c.getDescription() : c.getChargeType().name()) + " — " + c.getAmount(),
                        "Billing", c.getId())));

        events.sort(Comparator.comparing(TimelineEventDto::getTimestamp).reversed());
        return events;
    }

    private static TimelineEventDto buildEvent(String eventType, Instant timestamp, String title, String description, String sourceModule, Long referenceId) {
        TimelineEventDto dto = new TimelineEventDto();
        dto.setEventType(eventType);
        dto.setTimestamp(timestamp);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setSourceModule(sourceModule);
        dto.setReferenceId(referenceId);
        return dto;
    }

    private static Instant toInstant(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(DEFAULT_ZONE).toInstant();
    }

    private static String formatVitals(VitalSignRecord v) {
        StringBuilder sb = new StringBuilder();
        if (v.getBloodPressureSystolic() != null && v.getBloodPressureDiastolic() != null) {
            sb.append("BP ").append(v.getBloodPressureSystolic()).append("/").append(v.getBloodPressureDiastolic());
        }
        if (v.getPulse() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Pulse ").append(v.getPulse());
        }
        if (v.getTemperature() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Temp ").append(v.getTemperature());
        }
        return sb.length() > 0 ? sb.toString() : "Vital signs recorded";
    }
}
