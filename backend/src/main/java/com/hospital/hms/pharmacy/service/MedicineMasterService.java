package com.hospital.hms.pharmacy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.pharmacy.dto.MedicineMasterRequestDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterResponseDto;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.MedicineMasterAuditLog;
import com.hospital.hms.pharmacy.exception.DuplicateMedicineCodeException;
import com.hospital.hms.pharmacy.repository.MedicineMasterAuditLogRepository;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineMasterService {

    private final MedicineMasterRepository medicineRepository;
    private final MedicineMasterAuditLogRepository auditRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MedicineMasterService(MedicineMasterRepository medicineRepository,
                                 MedicineMasterAuditLogRepository auditRepository) {
        this.medicineRepository = medicineRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public MedicineMasterResponseDto create(MedicineMasterRequestDto request, String performedBy) {
        if (medicineRepository.existsByMedicineCodeIgnoreCase(request.getMedicineCode())) {
            throw new DuplicateMedicineCodeException("Medicine code already exists: " + request.getMedicineCode());
        }
        MedicineMaster entity = new MedicineMaster();
        applyRequest(request, entity);
        entity.setCreatedByUser(performedBy);
        entity = medicineRepository.save(entity);

        writeAudit(entity.getId(), "CREATE", null, entity, performedBy);
        return toDto(entity);
    }

    @Transactional
    public MedicineMasterResponseDto update(Long id, MedicineMasterRequestDto request, String performedBy) {
        MedicineMaster entity = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));
        MedicineMaster before = cloneForAudit(entity);
        applyRequest(request, entity);
        entity = medicineRepository.save(entity);
        writeAudit(id, "UPDATE", before, entity, performedBy);
        return toDto(entity);
    }

    @Transactional
    public void softDelete(Long id, String performedBy) {
        MedicineMaster entity = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));
        MedicineMaster before = cloneForAudit(entity);
        entity.setActive(false);
        medicineRepository.save(entity);
        writeAudit(id, "DISABLE", before, entity, performedBy);
    }

    @Transactional(readOnly = true)
    public List<MedicineMasterResponseDto> listAll() {
        return medicineRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void applyRequest(MedicineMasterRequestDto request, MedicineMaster entity) {
        entity.setMedicineCode(request.getMedicineCode().trim());
        entity.setMedicineName(request.getMedicineName().trim());
        entity.setCategory(request.getCategory());
        entity.setStrength(request.getStrength() != null ? request.getStrength().trim() : null);
        entity.setForm(request.getForm());
        entity.setMinStock(request.getMinStock());
        entity.setLasaFlag(request.getLasaFlag());
        entity.setStorageType(request.getStorageType());
        entity.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        entity.setManufacturer(request.getManufacturer() != null ? request.getManufacturer().trim() : null);
        entity.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
    }

    private MedicineMasterResponseDto toDto(MedicineMaster m) {
        MedicineMasterResponseDto dto = new MedicineMasterResponseDto();
        dto.setId(m.getId());
        dto.setMedicineCode(m.getMedicineCode());
        dto.setMedicineName(m.getMedicineName());
        dto.setCategory(m.getCategory());
        dto.setStrength(m.getStrength());
        dto.setForm(m.getForm());
        dto.setMinStock(m.getMinStock());
        dto.setLasaFlag(m.getLasaFlag());
        dto.setStorageType(m.getStorageType());
        dto.setActive(m.getActive());
        dto.setManufacturer(m.getManufacturer());
        dto.setNotes(m.getNotes());
        dto.setCreatedByUser(m.getCreatedByUser());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }

    private void writeAudit(Long medicineId, String action, MedicineMaster before, MedicineMaster after, String performedBy) {
        MedicineMasterAuditLog log = new MedicineMasterAuditLog();
        log.setMedicineId(medicineId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        try {
            if (before != null) {
                log.setOldValue(objectMapper.writeValueAsString(toDto(before)));
            }
            if (after != null) {
                log.setNewValue(objectMapper.writeValueAsString(toDto(after)));
            }
        } catch (JsonProcessingException e) {
            // Fallback: store minimal text; do not break transaction for audit serialisation
            log.setOldValue(before != null ? before.getMedicineCode() : null);
            log.setNewValue(after != null ? after.getMedicineCode() : null);
        }
        auditRepository.save(log);
    }

    private MedicineMaster cloneForAudit(MedicineMaster src) {
        MedicineMaster m = new MedicineMaster();
        m.setId(src.getId());
        m.setMedicineCode(src.getMedicineCode());
        m.setMedicineName(src.getMedicineName());
        m.setCategory(src.getCategory());
        m.setStrength(src.getStrength());
        m.setForm(src.getForm());
        m.setMinStock(src.getMinStock());
        m.setLasaFlag(src.getLasaFlag());
        m.setStorageType(src.getStorageType());
        m.setActive(src.getActive());
        m.setManufacturer(src.getManufacturer());
        m.setNotes(src.getNotes());
        m.setCreatedByUser(src.getCreatedByUser());
        m.setCreatedAt(src.getCreatedAt());
        m.setUpdatedAt(src.getUpdatedAt());
        return m;
    }
}

