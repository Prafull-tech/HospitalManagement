package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.dto.AdmissionPriorityItemDto;
import com.hospital.hms.ipd.entity.AdmissionPriority;
import com.hospital.hms.ipd.repository.AdmissionPriorityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Admission Priority REST API (list master). DB-agnostic.
 * Evaluation and override are delegated to AdmissionPriorityEvaluationService and IPDAdmissionService.
 */
@Service
public class AdmissionPriorityApiService {

    private final AdmissionPriorityRepository admissionPriorityRepository;

    public AdmissionPriorityApiService(AdmissionPriorityRepository admissionPriorityRepository) {
        this.admissionPriorityRepository = admissionPriorityRepository;
    }

    @Transactional(readOnly = true)
    public List<AdmissionPriorityItemDto> list(Boolean activeOnly) {
        List<AdmissionPriority> list = Boolean.TRUE.equals(activeOnly)
                ? admissionPriorityRepository.findByActiveTrueOrderByPriorityOrderAsc()
                : admissionPriorityRepository.findAllByOrderByPriorityOrderAsc();
        return list.stream().map(AdmissionPriorityApiService::toItemDto).collect(Collectors.toList());
    }

    private static AdmissionPriorityItemDto toItemDto(AdmissionPriority e) {
        AdmissionPriorityItemDto dto = new AdmissionPriorityItemDto();
        dto.setId(e.getId());
        dto.setPriorityCode(e.getPriorityCode());
        dto.setCategory(e.getCategory());
        dto.setDescription(e.getDescription());
        dto.setPriorityOrder(e.getPriorityOrder());
        dto.setActive(e.getActive());
        return dto;
    }
}
