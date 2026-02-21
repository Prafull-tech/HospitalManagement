package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.dto.RackSuggestionResponseDto;
import com.hospital.hms.pharmacy.entity.MedicineCategory;
import com.hospital.hms.pharmacy.entity.PharmacyRack;
import com.hospital.hms.pharmacy.entity.PharmacyShelf;
import com.hospital.hms.pharmacy.entity.RackCategoryType;
import com.hospital.hms.pharmacy.entity.RackSuggestionAuditLog;
import com.hospital.hms.pharmacy.entity.StorageType;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.pharmacy.repository.PharmacyRackRepository;
import com.hospital.hms.pharmacy.repository.PharmacyShelfRepository;
import com.hospital.hms.pharmacy.repository.RackSuggestionAuditLogRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Auto rack suggestion engine for pharmacy storage.
 * LASA-safe, cold-chain protected, capacity-aware.
 */
@Service
public class RackSuggestionService {

    private final PharmacyRackRepository rackRepository;
    private final PharmacyShelfRepository shelfRepository;
    private final MedicineMasterRepository medicineRepository;
    private final RackSuggestionAuditLogRepository auditRepository;

    public RackSuggestionService(PharmacyRackRepository rackRepository,
                                 PharmacyShelfRepository shelfRepository,
                                 MedicineMasterRepository medicineRepository,
                                 RackSuggestionAuditLogRepository auditRepository) {
        this.rackRepository = rackRepository;
        this.shelfRepository = shelfRepository;
        this.medicineRepository = medicineRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Suggest best rack/shelf for medicine based on category, storage type, LASA flag.
     */
    @Transactional(readOnly = true)
    public Optional<RackSuggestionResponseDto> suggest(String category, String storageType, boolean lasaFlag) {
        StorageType st = parseStorageType(storageType);
        RackCategoryType targetCategory = mapCategoryToRackType(category);

        List<PharmacyRack> racks = rackRepository.findByActiveTrueAndStorageTypeOrderByRackCodeAsc(st);
        if (racks.isEmpty()) {
            return Optional.empty();
        }

        List<RackWithLoad> withLoad = racks.stream()
                .map(r -> new RackWithLoad(r, medicineRepository.countActiveByRackId(r.getId()),
                        medicineRepository.countLasaByRackId(r.getId()),
                        r.getMaxCapacity() != null ? r.getMaxCapacity() : 100))
                .collect(Collectors.toList());

        // Filter by category match (null categoryType = GENERAL, matches any)
        List<RackWithLoad> categoryMatch = withLoad.stream()
                .filter(rw -> matchesCategory(rw.rack.getCategoryType(), targetCategory))
                .collect(Collectors.toList());

        List<RackWithLoad> candidates = categoryMatch.isEmpty() ? withLoad : categoryMatch;

        // LASA: prefer lasaSafe racks; avoid racks with many LASA medicines if not lasaSafe
        if (lasaFlag) {
            candidates = candidates.stream()
                    .filter(rw -> Boolean.TRUE.equals(rw.rack.getLasaSafe()) || rw.lasaCount == 0)
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                candidates = withLoad.stream()
                        .filter(rw -> rw.lasaCount == 0)
                        .collect(Collectors.toList());
            }
            if (candidates.isEmpty()) {
                candidates = withLoad;
            }
        } else {
            candidates = candidates.stream()
                    .filter(rw -> !Boolean.TRUE.equals(rw.rack.getLasaSafe()))
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                candidates = withLoad;
            }
        }

        // Choose: lowest load, highest capacity left
        Optional<RackWithLoad> best = candidates.stream()
                .min(Comparator
                        .comparingLong((RackWithLoad rw) -> rw.currentLoad)
                        .thenComparingLong((RackWithLoad rw) -> -(rw.maxCapacity - rw.currentLoad)));

        return best.flatMap(rw -> pickShelf(rw).map(shelf -> toResponse(rw.rack, shelf, st, targetCategory, lasaFlag)));
    }

    /**
     * Log rack suggestion audit (call when user accepts or overrides).
     */
    @Transactional
    public void logSuggestionAudit(Long suggestedRackId, String suggestedRackCode,
                                   Long finalRackId, String finalRackCode,
                                   boolean userOverride, String category, String storageType, boolean lasaFlag) {
        String performedBy = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        RackSuggestionAuditLog log = new RackSuggestionAuditLog();
        log.setSuggestedRackId(suggestedRackId);
        log.setSuggestedRackCode(suggestedRackCode);
        log.setFinalRackId(finalRackId);
        log.setFinalRackCode(finalRackCode);
        log.setUserOverride(userOverride);
        log.setMedicineCategory(category);
        log.setStorageType(storageType);
        log.setLasaFlag(lasaFlag);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        log.setCorrelationId(correlationId);
        auditRepository.save(log);
    }

    private boolean matchesCategory(RackCategoryType rackCat, RackCategoryType target) {
        if (rackCat == null) return true;
        if (target == RackCategoryType.GENERAL) return true;
        return rackCat == target;
    }

    private RackCategoryType mapCategoryToRackType(String category) {
        if (category == null || category.isBlank()) return RackCategoryType.GENERAL;
        try {
            MedicineCategory mc = MedicineCategory.valueOf(category.toUpperCase().replace(" ", "_"));
            return switch (mc) {
                case ANTIBIOTIC -> RackCategoryType.ANTIBIOTIC;
                case CARDIAC -> RackCategoryType.CARDIAC;
                case ICU_EMERGENCY -> RackCategoryType.ICU_EMERGENCY;
                default -> RackCategoryType.GENERAL;
            };
        } catch (IllegalArgumentException e) {
            return RackCategoryType.GENERAL;
        }
    }

    private StorageType parseStorageType(String s) {
        if (s == null || s.isBlank()) return StorageType.ROOM_TEMP;
        if ("COLD_CHAIN".equalsIgnoreCase(s) || "COLDCHAIN".equalsIgnoreCase(s)) return StorageType.COLD_CHAIN;
        return StorageType.ROOM_TEMP;
    }

    private Optional<PharmacyShelf> pickShelf(RackWithLoad rw) {
        List<PharmacyShelf> shelves = shelfRepository.findByRack_IdAndActiveTrueOrderByShelfLevelAsc(rw.rack.getId());
        if (shelves.isEmpty()) {
            shelves = shelfRepository.findByRack_IdOrderByShelfLevelAsc(rw.rack.getId());
        }
        return shelves.isEmpty() ? Optional.empty() : Optional.of(shelves.get(0));
    }

    private RackSuggestionResponseDto toResponse(PharmacyRack rack, PharmacyShelf shelf,
                                                  StorageType storageType, RackCategoryType category, boolean lasaFlag) {
        RackSuggestionResponseDto dto = new RackSuggestionResponseDto();
        dto.setRackId(rack.getId());
        dto.setRackCode(rack.getRackCode());
        dto.setRackName(rack.getRackName());
        dto.setShelfId(shelf.getId());
        dto.setShelfCode(shelf.getShelfCode());
        String catStr = category != null ? category.name().replace("_", " ") : "General";
        String storageStr = storageType == StorageType.COLD_CHAIN ? "Cold Chain" : "Room Temp";
        String lasaStr = lasaFlag ? " (LASA-safe)" : "";
        dto.setReason(catStr + " Rack (" + storageStr + ")" + lasaStr);
        return dto;
    }

    private record RackWithLoad(PharmacyRack rack, long currentLoad, long lasaCount, int maxCapacity) {}
}
