package com.hospital.hms.pharmacy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.dto.*;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.MedicineMasterAuditLog;
import com.hospital.hms.pharmacy.entity.MedicineEntryAuditLog;
import com.hospital.hms.pharmacy.entity.PharmacyRack;
import com.hospital.hms.pharmacy.entity.PharmacyShelf;
import com.hospital.hms.pharmacy.exception.DuplicateMedicineCodeException;
import com.hospital.hms.pharmacy.repository.MedicineMasterAuditLogRepository;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.pharmacy.repository.MedicineEntryAuditLogRepository;
import com.hospital.hms.pharmacy.repository.PharmacyRackRepository;
import com.hospital.hms.pharmacy.repository.PharmacyShelfRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicineMasterService {

    private final MedicineMasterRepository medicineRepository;
    private final MedicineMasterAuditLogRepository auditRepository;
    private final MedicineEntryAuditLogRepository entryAuditRepository;
    private final PharmacyRackRepository rackRepository;
    private final PharmacyShelfRepository shelfRepository;
    private final StockTransactionService stockTransactionService;
    private final MedicineLookupService medicineLookupService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MedicineMasterService(MedicineMasterRepository medicineRepository,
                                 MedicineMasterAuditLogRepository auditRepository,
                                 MedicineEntryAuditLogRepository entryAuditRepository,
                                 PharmacyRackRepository rackRepository,
                                 PharmacyShelfRepository shelfRepository,
                                 StockTransactionService stockTransactionService,
                                 MedicineLookupService medicineLookupService) {
        this.medicineRepository = medicineRepository;
        this.auditRepository = auditRepository;
        this.entryAuditRepository = entryAuditRepository;
        this.rackRepository = rackRepository;
        this.shelfRepository = shelfRepository;
        this.stockTransactionService = stockTransactionService;
        this.medicineLookupService = medicineLookupService;
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

    @Transactional(readOnly = true)
    public List<MedicineMasterResponseDto> search(String q) {
        if (q == null || q.isBlank()) {
            return listAll();
        }
        return medicineRepository.searchActiveByNameOrCode(q.trim()).stream()
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
        entity.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0);
        entity.setLasaFlag(request.getLasaFlag());
        entity.setStorageType(request.getStorageType());
        entity.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        entity.setManufacturer(request.getManufacturer() != null ? request.getManufacturer().trim() : null);
        entity.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        entity.setBinNumber(request.getBinNumber() != null ? request.getBinNumber().trim() : null);
        entity.setBarcode(request.getBarcode() != null ? request.getBarcode().trim() : null);

        PharmacyRack rack = null;
        PharmacyShelf shelf = null;
        if (request.getRackId() != null) {
            rack = rackRepository.findById(request.getRackId())
                    .orElseThrow(() -> new IllegalArgumentException("Rack not found: " + request.getRackId()));
            if (!rack.getStorageType().equals(request.getStorageType())) {
                throw new IllegalArgumentException("Medicine storage type " + request.getStorageType()
                        + " does not match rack storage type " + rack.getStorageType()
                        + ". Cold chain medicine cannot be mapped to room temp rack.");
            }
            if (request.getShelfId() != null) {
                shelf = shelfRepository.findById(request.getShelfId())
                        .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + request.getShelfId()));
                if (!shelf.getRack().getId().equals(rack.getId())) {
                    throw new IllegalArgumentException("Shelf does not belong to the selected rack.");
                }
            }
        }
        entity.setRack(rack);
        entity.setShelf(shelf);
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
        dto.setQuantity(m.getQuantity() != null ? m.getQuantity() : 0);
        dto.setLasaFlag(m.getLasaFlag());
        dto.setStorageType(m.getStorageType());
        dto.setActive(m.getActive());
        dto.setManufacturer(m.getManufacturer());
        dto.setNotes(m.getNotes());
        dto.setCreatedByUser(m.getCreatedByUser());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setBinNumber(m.getBinNumber());
        if (m.getRack() != null) {
            dto.setRackId(m.getRack().getId());
            dto.setRackCode(m.getRack().getRackCode());
        }
        if (m.getShelf() != null) {
            dto.setShelfId(m.getShelf().getId());
            dto.setShelfCode(m.getShelf().getShelfCode());
        }
        dto.setBarcode(m.getBarcode());
        return dto;
    }

    @Transactional(readOnly = true)
    public Optional<MedicineMasterResponseDto> findByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }
        return medicineRepository.findByBarcodeIgnoreCase(barcode.trim())
                .map(this::toDto);
    }

    @Transactional
    public MedicineMasterResponseDto createManual(ManualEntryRequestDto request, String performedBy) {
        String correlationId = resolveCorrelationId();
        MedicineMasterRequestDto med = request.getMedicine();
        if (medicineRepository.existsByMedicineCodeIgnoreCase(med.getMedicineCode())) {
            throw new DuplicateMedicineCodeException("Medicine code already exists: " + med.getMedicineCode());
        }
        MedicineMaster entity = new MedicineMaster();
        applyRequest(med, entity);
        entity.setQuantity(0);
        entity.setCreatedByUser(performedBy);
        entity = medicineRepository.save(entity);
        writeAudit(entity.getId(), "CREATE", null, entity, performedBy);

        int qty = request.getQuantity() != null ? request.getQuantity() : 0;
        if (qty > 0) {
            PurchaseRequestDto purchase = new PurchaseRequestDto();
            purchase.setMedicineId(entity.getId());
            purchase.setQuantity(qty);
            purchase.setTransactionDate(LocalDate.now());
            purchase.setBatchNumber(request.getBatchNumber());
            purchase.setExpiryDate(request.getExpiryDate());
            stockTransactionService.purchase(purchase, performedBy);
            entity = medicineRepository.findById(entity.getId()).orElse(entity);
        }

        writeEntryAudit("MANUAL", null, null, entity.getId(), performedBy, correlationId);
        if (request.getExternalLookupBarcode() != null && !request.getExternalLookupBarcode().isBlank()) {
            medicineLookupService.logSavedLocally(request.getExternalLookupBarcode().trim(), entity.getId());
        }
        return toDto(entity);
    }

    @Transactional
    public MedicineMasterResponseDto createFromBarcode(BarcodeEntryRequestDto request, String performedBy) {
        String correlationId = resolveCorrelationId();
        Optional<MedicineMaster> existing = medicineRepository.findByBarcodeIgnoreCase(request.getBarcode().trim());

        if (existing.isPresent()) {
            MedicineMaster m = existing.get();
            PurchaseRequestDto purchase = new PurchaseRequestDto();
            purchase.setMedicineId(m.getId());
            purchase.setQuantity(request.getQuantity());
            purchase.setTransactionDate(LocalDate.now());
            purchase.setBatchNumber(request.getBatchNumber());
            purchase.setExpiryDate(request.getExpiryDate());
            stockTransactionService.purchase(purchase, performedBy);
            writeEntryAudit("BARCODE", request.getBarcode(), null, m.getId(), performedBy, correlationId);
            return toDto(medicineRepository.findById(m.getId()).orElse(m));
        }

        MedicineMasterRequestDto createReq = request.getCreateNewMedicine();
        if (createReq == null) {
            throw new IllegalArgumentException("Medicine not found for barcode. Provide createNewMedicine to create.");
        }
        if (medicineRepository.existsByMedicineCodeIgnoreCase(createReq.getMedicineCode())) {
            throw new DuplicateMedicineCodeException("Medicine code already exists: " + createReq.getMedicineCode());
        }
        createReq.setBarcode(request.getBarcode().trim());
        MedicineMaster entity = new MedicineMaster();
        applyRequest(createReq, entity);
        entity.setQuantity(0);
        entity.setCreatedByUser(performedBy);
        entity = medicineRepository.save(entity);
        writeAudit(entity.getId(), "CREATE", null, entity, performedBy);

        if (request.getQuantity() != null && request.getQuantity() > 0) {
            PurchaseRequestDto purchase = new PurchaseRequestDto();
            purchase.setMedicineId(entity.getId());
            purchase.setQuantity(request.getQuantity());
            purchase.setTransactionDate(LocalDate.now());
            purchase.setBatchNumber(request.getBatchNumber());
            purchase.setExpiryDate(request.getExpiryDate());
            stockTransactionService.purchase(purchase, performedBy);
            entity = medicineRepository.findById(entity.getId()).orElse(entity);
        }

        writeEntryAudit("BARCODE", request.getBarcode(), null, entity.getId(), performedBy, correlationId);
        if (Boolean.TRUE.equals(request.getFromExternalLookup())) {
            medicineLookupService.logSavedLocally(request.getBarcode().trim(), entity.getId());
        }
        return toDto(medicineRepository.findById(entity.getId()).orElse(entity));
    }

    @Transactional
    public MedicineMasterResponseDto addBatchToExisting(ExistingMedicineBatchRequestDto request, String performedBy) {
        String correlationId = resolveCorrelationId();
        MedicineMaster m = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + request.getMedicineId()));

        if (request.getRackId() != null) {
            PharmacyRack rack = rackRepository.findById(request.getRackId())
                    .orElseThrow(() -> new IllegalArgumentException("Rack not found: " + request.getRackId()));
            if (!rack.getStorageType().equals(m.getStorageType())) {
                throw new IllegalArgumentException("Medicine storage type " + m.getStorageType()
                        + " does not match rack storage type " + rack.getStorageType());
            }
            m.setRack(rack);
            PharmacyShelf shelf = null;
            if (request.getShelfId() != null) {
                shelf = shelfRepository.findById(request.getShelfId())
                        .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + request.getShelfId()));
                if (!shelf.getRack().getId().equals(rack.getId())) {
                    throw new IllegalArgumentException("Shelf does not belong to selected rack");
                }
                m.setShelf(shelf);
            } else {
                m.setShelf(null);
            }
            medicineRepository.save(m);
        }

        PurchaseRequestDto purchase = new PurchaseRequestDto();
        purchase.setMedicineId(request.getMedicineId());
        purchase.setQuantity(request.getQuantity());
        purchase.setTransactionDate(LocalDate.now());
        purchase.setBatchNumber(request.getBatchNumber());
        purchase.setExpiryDate(request.getExpiryDate());
        stockTransactionService.purchase(purchase, performedBy);

        writeEntryAudit("EXISTING", null, null, m.getId(), performedBy, correlationId);
        return toDto(medicineRepository.findById(m.getId()).orElse(m));
    }

    private void writeEntryAudit(String entryMode, String barcode, String excelFilename, Long medicineId,
                                 String performedBy, String correlationId) {
        MedicineEntryAuditLog log = new MedicineEntryAuditLog();
        log.setEntryMode(entryMode);
        log.setBarcode(barcode);
        log.setExcelFilename(excelFilename);
        log.setMedicineId(medicineId);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        log.setCorrelationId(correlationId);
        entryAuditRepository.save(log);
    }

    private String resolveCorrelationId() {
        String cid = MDC.get(MdcKeys.CORRELATION_ID);
        return cid != null && !cid.isBlank() ? cid : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
        m.setQuantity(src.getQuantity());
        m.setLasaFlag(src.getLasaFlag());
        m.setStorageType(src.getStorageType());
        m.setActive(src.getActive());
        m.setManufacturer(src.getManufacturer());
        m.setNotes(src.getNotes());
        m.setCreatedByUser(src.getCreatedByUser());
        m.setCreatedAt(src.getCreatedAt());
        m.setUpdatedAt(src.getUpdatedAt());
        m.setRack(src.getRack());
        m.setShelf(src.getShelf());
        m.setBinNumber(src.getBinNumber());
        m.setBarcode(src.getBarcode());
        return m;
    }
}

