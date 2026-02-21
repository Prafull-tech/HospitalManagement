package com.hospital.hms.pharmacy.service;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.pharmacy.dto.PatientIpdStatusDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Pharmacy patient lookup for sell (stock out) - IPD/OPD status.
 */
@Service
public class PharmacyPatientService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.ACTIVE,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final IPDAdmissionRepository admissionRepository;
    private final BedAllocationRepository bedAllocationRepository;

    public PharmacyPatientService(IPDAdmissionRepository admissionRepository,
                                  BedAllocationRepository bedAllocationRepository) {
        this.admissionRepository = admissionRepository;
        this.bedAllocationRepository = bedAllocationRepository;
    }

    @Transactional(readOnly = true)
    public PatientIpdStatusDto getActiveIpdStatus(Long patientId) {
        if (patientId == null) return emptyStatus();
        List<IPDAdmission> active = admissionRepository.findByPatientIdAndAdmissionStatusIn(patientId, ACTIVE_STATUSES);
        if (active.isEmpty()) {
            PatientIpdStatusDto dto = emptyStatus();
            dto.setIpdLinked(false);
            return dto;
        }
        IPDAdmission adm = active.get(0);
        PatientIpdStatusDto dto = new PatientIpdStatusDto();
        dto.setIpdAdmissionId(adm.getId());
        dto.setAdmissionNumber(adm.getAdmissionNumber());
        dto.setIpdLinked(true);
        bedAllocationRepository.findActiveByAdmissionIdWithBedAndRoom(adm.getId()).ifPresent(alloc -> {
            dto.setBedNumber(alloc.getBed().getBedNumber());
            if (alloc.getBed().getWard() != null) {
                dto.setWardName(alloc.getBed().getWard().getName());
            }
        });
        return dto;
    }

    private static PatientIpdStatusDto emptyStatus() {
        PatientIpdStatusDto dto = new PatientIpdStatusDto();
        dto.setIpdLinked(false);
        return dto;
    }
}
