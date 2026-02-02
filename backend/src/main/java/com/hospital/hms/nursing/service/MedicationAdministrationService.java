package com.hospital.hms.nursing.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.nursing.dto.MedicationAdministrationRequestDto;
import com.hospital.hms.nursing.dto.MedicationAdministrationResponseDto;
import com.hospital.hms.nursing.entity.MedicationAdministration;
import com.hospital.hms.nursing.entity.NursingStaff;
import com.hospital.hms.nursing.repository.MedicationAdministrationRepository;
import com.hospital.hms.nursing.repository.NursingStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Medication Administration Record (MAR) service. Only active IPD admissions. DB-agnostic.
 */
@Service
public class MedicationAdministrationService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final MedicationAdministrationRepository marRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final NursingStaffRepository staffRepository;

    public MedicationAdministrationService(MedicationAdministrationRepository marRepository,
                                            IPDAdmissionRepository admissionRepository,
                                            NursingStaffRepository staffRepository) {
        this.marRepository = marRepository;
        this.admissionRepository = admissionRepository;
        this.staffRepository = staffRepository;
    }

    @Transactional
    public MedicationAdministrationResponseDto record(MedicationAdministrationRequestDto request) {
        IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Medication can only be recorded for active IPD admissions. Current status: " + admission.getAdmissionStatus());
        }
        MedicationAdministration mar = new MedicationAdministration();
        mar.setIpdAdmission(admission);
        mar.setMedicationName(request.getMedicationName().trim());
        mar.setDosage(request.getDosage() != null ? request.getDosage().trim() : null);
        mar.setRoute(request.getRoute() != null ? request.getRoute().trim() : null);
        LocalDateTime administeredAt = request.getAdministeredAt() != null ? request.getAdministeredAt() : LocalDateTime.now();
        mar.setAdministeredAt(administeredAt);
        mar.setDoctorOrderRef(request.getDoctorOrderRef() != null ? request.getDoctorOrderRef().trim() : null);
        mar.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);
        if (request.getAdministeredById() != null) {
            NursingStaff staff = staffRepository.findById(request.getAdministeredById()).orElse(null);
            mar.setAdministeredBy(staff);
        }
        mar = marRepository.save(mar);
        return toDto(mar);
    }

    public List<MedicationAdministrationResponseDto> findByIpdAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        return marRepository.findByIpdAdmissionIdOrderByAdministeredAtDesc(ipdAdmissionId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MedicationAdministrationResponseDto toDto(MedicationAdministration m) {
        MedicationAdministrationResponseDto dto = new MedicationAdministrationResponseDto();
        dto.setId(m.getId());
        dto.setIpdAdmissionId(m.getIpdAdmission().getId());
        dto.setMedicationName(m.getMedicationName());
        dto.setDosage(m.getDosage());
        dto.setRoute(m.getRoute());
        dto.setAdministeredAt(m.getAdministeredAt());
        dto.setDoctorOrderRef(m.getDoctorOrderRef());
        dto.setRemarks(m.getRemarks());
        dto.setCreatedAt(m.getCreatedAt());
        if (m.getAdministeredBy() != null) {
            dto.setAdministeredById(m.getAdministeredBy().getId());
            dto.setAdministeredByName(m.getAdministeredBy().getFullName());
        }
        return dto;
    }
}
