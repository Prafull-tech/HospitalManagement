package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.client.ThirdPartyDrugApiClient;
import com.hospital.hms.pharmacy.dto.ExternalDrugApiResponseDto;
import com.hospital.hms.pharmacy.dto.MedicineLookupResponseDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterRequestDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterResponseDto;
import com.hospital.hms.pharmacy.entity.MedicineCategory;
import com.hospital.hms.pharmacy.entity.MedicineForm;
import com.hospital.hms.pharmacy.entity.MedicineLookupAuditLog;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.StorageType;
import com.hospital.hms.pharmacy.repository.MedicineLookupAuditLogRepository;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Medicine lookup by barcode/GTIN.
 * Local-first: check Medicine Master, then 3rd party API.
 * NABH audit-ready.
 */
@Service
public class MedicineLookupService {

    private final MedicineMasterRepository medicineRepository;
    private final MedicineLookupAuditLogRepository lookupAuditRepository;
    private final ThirdPartyDrugApiClient externalApiClient;

    public MedicineLookupService(MedicineMasterRepository medicineRepository,
                                 MedicineLookupAuditLogRepository lookupAuditRepository,
                                 ThirdPartyDrugApiClient externalApiClient) {
        this.medicineRepository = medicineRepository;
        this.lookupAuditRepository = lookupAuditRepository;
        this.externalApiClient = externalApiClient;
    }

    /**
     * Look up medicine by barcode. Local first, then external.
     * Returns empty if not found anywhere.
     */
    @Transactional
    public Optional<MedicineLookupResponseDto> lookup(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }
        String trimmed = barcode.trim();

        // Step 1: Check local DB
        Optional<MedicineMaster> local = medicineRepository.findByBarcodeIgnoreCase(trimmed);
        if (local.isPresent()) {
            MedicineLookupResponseDto dto = new MedicineLookupResponseDto();
            dto.setSource(MedicineLookupResponseDto.LookupSource.LOCAL);
            dto.setData(toDto(local.get()));
            logLookup(trimmed, "LOCAL", false, null);
            return Optional.of(dto);
        }

        // Step 2: Call external API
        Optional<ExternalDrugApiResponseDto> external = externalApiClient.lookupByBarcode(trimmed);
        if (external.isPresent()) {
            MedicineMasterResponseDto mapped = mapExternalToDto(external.get(), trimmed);
            MedicineLookupResponseDto dto = new MedicineLookupResponseDto();
            dto.setSource(MedicineLookupResponseDto.LookupSource.EXTERNAL);
            dto.setData(mapped);
            logLookup(trimmed, "EXTERNAL", true, null);
            return Optional.of(dto);
        }

        logLookup(trimmed, "NOT_FOUND", false, null);
        return Optional.empty();
    }

    /**
     * Log when medicine is saved locally (after external lookup).
     */
    @Transactional
    public void logSavedLocally(String barcode, Long medicineId) {
        String performedBy = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        MedicineLookupAuditLog log = new MedicineLookupAuditLog();
        log.setBarcode(barcode);
        log.setLookupSource("EXTERNAL");
        log.setExternalLookupUsed(true);
        log.setSavedLocally(true);
        log.setMedicineId(medicineId);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        log.setCorrelationId(correlationId);
        lookupAuditRepository.save(log);
    }

    private void logLookup(String barcode, String source, boolean externalUsed, Long medicineId) {
        try {
            String performedBy = SecurityContextUserResolver.resolveUserId();
            String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }

            MedicineLookupAuditLog log = new MedicineLookupAuditLog();
            log.setBarcode(barcode);
            log.setLookupSource(source);
            log.setExternalLookupUsed(externalUsed);
            log.setSavedLocally(false);
            log.setMedicineId(medicineId);
            log.setPerformedBy(performedBy);
            log.setPerformedAt(Instant.now());
            log.setCorrelationId(correlationId);
            lookupAuditRepository.save(log);
        } catch (Exception e) {
            // Audit must not break lookup
        }
    }

    private MedicineMasterResponseDto mapExternalToDto(ExternalDrugApiResponseDto ext, String barcode) {
        MedicineMasterResponseDto dto = new MedicineMasterResponseDto();
        dto.setId(null);
        String code = barcode.replaceAll("\\s+", "_");
        dto.setMedicineCode(code.length() > 50 ? code.substring(0, 50) : code);
        dto.setMedicineName(ext.resolveMedicineName());
        dto.setStrength(ext.getStrength() != null ? ext.getStrength().trim() : null);
        dto.setForm(mapForm(ext.getForm()));
        dto.setManufacturer(ext.getManufacturer() != null ? ext.getManufacturer().trim() : null);
        dto.setCategory(mapCategory(ext.getCategory()));
        dto.setMinStock(0);
        dto.setQuantity(0);
        dto.setLasaFlag(false);
        dto.setStorageType(StorageType.ROOM_TEMP);
        dto.setActive(true);
        dto.setBarcode(barcode);
        return dto;
    }

    private MedicineCategory mapCategory(String cat) {
        if (cat == null || cat.isBlank()) return MedicineCategory.OTHER;
        String u = cat.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return MedicineCategory.valueOf(u);
        } catch (IllegalArgumentException e) {
            if (u.contains("ANTIBIOTIC")) return MedicineCategory.ANTIBIOTIC;
            if (u.contains("CARDIAC")) return MedicineCategory.CARDIAC;
            if (u.contains("DIABETIC")) return MedicineCategory.DIABETIC;
            if (u.contains("IV") || u.contains("FLUID")) return MedicineCategory.IV_FLUID;
            if (u.contains("ICU") || u.contains("EMERGENCY")) return MedicineCategory.ICU_EMERGENCY;
            if (u.contains("ANALGESIC") || u.contains("PAIN")) return MedicineCategory.ANALGESIC;
            return MedicineCategory.OTHER;
        }
    }

    private MedicineForm mapForm(String form) {
        if (form == null || form.isBlank()) return MedicineForm.OTHER;
        String u = form.toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return MedicineForm.valueOf(u);
        } catch (IllegalArgumentException e) {
            if (u.contains("TABLET")) return MedicineForm.TABLET;
            if (u.contains("CAPSULE")) return MedicineForm.CAPSULE;
            if (u.contains("INJECTION") || u.contains("INJ")) return MedicineForm.INJECTION;
            if (u.contains("IV") || u.contains("INFUSION")) return MedicineForm.IV;
            if (u.contains("SYRUP") || u.contains("SUSPENSION")) return MedicineForm.SYRUP;
            if (u.contains("OINTMENT") || u.contains("CREAM")) return MedicineForm.OINTMENT;
            return MedicineForm.OTHER;
        }
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
        dto.setQuantity(m.getQuantity());
        dto.setLasaFlag(m.getLasaFlag());
        dto.setStorageType(m.getStorageType());
        dto.setActive(m.getActive());
        dto.setManufacturer(m.getManufacturer());
        dto.setNotes(m.getNotes());
        dto.setBarcode(m.getBarcode());
        dto.setCreatedByUser(m.getCreatedByUser());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        if (m.getRack() != null) {
            dto.setRackId(m.getRack().getId());
            dto.setRackCode(m.getRack().getRackCode());
        }
        if (m.getShelf() != null) {
            dto.setShelfId(m.getShelf().getId());
            dto.setShelfCode(m.getShelf().getShelfCode());
        }
        dto.setBinNumber(m.getBinNumber());
        return dto;
    }
}
