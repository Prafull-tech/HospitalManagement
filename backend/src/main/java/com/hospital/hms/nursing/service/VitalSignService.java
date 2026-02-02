package com.hospital.hms.nursing.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.nursing.dto.VitalSignRequestDto;
import com.hospital.hms.nursing.dto.VitalSignResponseDto;
import com.hospital.hms.nursing.entity.NursingStaff;
import com.hospital.hms.nursing.entity.VitalSignRecord;
import com.hospital.hms.nursing.repository.NursingStaffRepository;
import com.hospital.hms.nursing.repository.VitalSignRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vital signs service. Only active IPD admissions; time-ordered vitals. DB-agnostic.
 */
@Service
public class VitalSignService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final VitalSignRecordRepository vitalSignRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final NursingStaffRepository staffRepository;

    public VitalSignService(VitalSignRecordRepository vitalSignRepository,
                            IPDAdmissionRepository admissionRepository,
                            NursingStaffRepository staffRepository) {
        this.vitalSignRepository = vitalSignRepository;
        this.admissionRepository = admissionRepository;
        this.staffRepository = staffRepository;
    }

    @Transactional
    public VitalSignResponseDto record(VitalSignRequestDto request) {
        IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Vital signs can only be recorded for active IPD admissions. Current status: " + admission.getAdmissionStatus());
        }
        VitalSignRecord vital = new VitalSignRecord();
        vital.setIpdAdmission(admission);
        LocalDateTime recordedAt = request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now();
        vital.setRecordedAt(recordedAt);
        vital.setBloodPressureSystolic(request.getBloodPressureSystolic());
        vital.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        vital.setPulse(request.getPulse());
        vital.setTemperature(request.getTemperature());
        vital.setSpo2(request.getSpo2());
        vital.setRespiration(request.getRespiration());
        vital.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);
        if (request.getRecordedById() != null) {
            NursingStaff staff = staffRepository.findById(request.getRecordedById())
                    .orElse(null);
            vital.setRecordedBy(staff);
        }
        vital = vitalSignRepository.save(vital);
        return toDto(vital);
    }

    public List<VitalSignResponseDto> getByIpdAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        return vitalSignRepository.findByIpdAdmissionIdOrderByRecordedAtDesc(ipdAdmissionId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private VitalSignResponseDto toDto(VitalSignRecord r) {
        VitalSignResponseDto dto = new VitalSignResponseDto();
        dto.setId(r.getId());
        dto.setIpdAdmissionId(r.getIpdAdmission().getId());
        dto.setRecordedAt(r.getRecordedAt());
        dto.setBloodPressureSystolic(r.getBloodPressureSystolic());
        dto.setBloodPressureDiastolic(r.getBloodPressureDiastolic());
        dto.setPulse(r.getPulse());
        dto.setTemperature(r.getTemperature());
        dto.setSpo2(r.getSpo2());
        dto.setRespiration(r.getRespiration());
        dto.setRemarks(r.getRemarks());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getRecordedBy() != null) {
            dto.setRecordedById(r.getRecordedBy().getId());
            dto.setRecordedByName(r.getRecordedBy().getFullName());
        }
        return dto;
    }
}
