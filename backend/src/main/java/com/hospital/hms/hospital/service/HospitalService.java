package com.hospital.hms.hospital.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.dto.HospitalRequestDto;
import com.hospital.hms.hospital.dto.HospitalResponseDto;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hospital service. Full CRUD, multi-hospital ready. DB-agnostic.
 */
@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional(readOnly = true)
    public List<HospitalResponseDto> list(Boolean activeOnly) {
        List<Hospital> list = Boolean.TRUE.equals(activeOnly)
                ? hospitalRepository.findByDeletedFalseAndIsActiveTrueOrderByHospitalNameAsc()
                : hospitalRepository.findByDeletedFalseOrderByHospitalNameAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HospitalResponseDto getById(Long id) {
        Hospital h = getEntityById(id);
        return toDto(h);
    }

    @Transactional(readOnly = true)
    public Hospital getEntityById(Long id) {
        Hospital h = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + id));
        if (Boolean.TRUE.equals(h.getDeleted())) {
            throw new ResourceNotFoundException("Hospital not found: " + id);
        }
        return h;
    }

    @Transactional
    public HospitalResponseDto create(HospitalRequestDto request) {
        String code = request.getHospitalCode() != null ? request.getHospitalCode().trim() : "";
        if (hospitalRepository.existsByHospitalCodeAndDeletedFalse(code)) {
            throw new IllegalArgumentException("Hospital code already exists: " + code);
        }
        Hospital h = new Hospital();
        h.setHospitalCode(code);
        h.setHospitalName(request.getHospitalName() != null ? request.getHospitalName().trim() : "");
        h.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        h.setIsActive(request.getActive() != null ? request.getActive() : true);
        h.setDeleted(false);
        h = hospitalRepository.save(h);
        return toDto(h);
    }

    @Transactional
    public HospitalResponseDto update(Long id, HospitalRequestDto request) {
        Hospital h = getEntityById(id);
        String code = request.getHospitalCode() != null ? request.getHospitalCode().trim() : "";
        if (hospitalRepository.existsByHospitalCodeAndDeletedFalseAndIdNot(code, id)) {
            throw new IllegalArgumentException("Hospital code already exists: " + code);
        }
        h.setHospitalCode(code);
        h.setHospitalName(request.getHospitalName() != null ? request.getHospitalName().trim() : "");
        h.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        h.setIsActive(request.getActive() != null ? request.getActive() : true);
        h = hospitalRepository.save(h);
        return toDto(h);
    }

    @Transactional
    public void delete(Long id) {
        Hospital h = getEntityById(id);
        h.setDeleted(true);
        h.setIsActive(false);
        hospitalRepository.save(h);
    }

    private HospitalResponseDto toDto(Hospital h) {
        HospitalResponseDto dto = new HospitalResponseDto();
        dto.setId(h.getId());
        dto.setHospitalCode(h.getHospitalCode());
        dto.setHospitalName(h.getHospitalName());
        dto.setLocation(h.getLocation());
        dto.setActive(h.getIsActive());
        return dto;
    }
}
