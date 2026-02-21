package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.dto.RackInventoryDto;
import com.hospital.hms.pharmacy.dto.RackInventoryItemDto;
import com.hospital.hms.pharmacy.dto.RackRequestDto;
import com.hospital.hms.pharmacy.dto.RackResponseDto;
import com.hospital.hms.pharmacy.dto.ShelfRequestDto;
import com.hospital.hms.pharmacy.dto.ShelfResponseDto;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.PharmacyRack;
import com.hospital.hms.pharmacy.entity.PharmacyShelf;
import com.hospital.hms.pharmacy.entity.StorageType;
import com.hospital.hms.pharmacy.exception.DuplicateRackCodeException;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.pharmacy.repository.PharmacyRackRepository;
import com.hospital.hms.pharmacy.repository.PharmacyShelfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rack and shelf management service. NABH audit-ready.
 */
@Service
public class PharmacyRackService {

    private static final Logger log = LoggerFactory.getLogger(PharmacyRackService.class);

    private final PharmacyRackRepository rackRepository;
    private final PharmacyShelfRepository shelfRepository;
    private final MedicineMasterRepository medicineRepository;

    public PharmacyRackService(PharmacyRackRepository rackRepository,
                               PharmacyShelfRepository shelfRepository,
                               MedicineMasterRepository medicineRepository) {
        this.rackRepository = rackRepository;
        this.shelfRepository = shelfRepository;
        this.medicineRepository = medicineRepository;
    }

    @Transactional
    public RackResponseDto createRack(RackRequestDto request, String performedBy) {
        if (rackRepository.existsByRackCodeIgnoreCase(request.getRackCode())) {
            throw new DuplicateRackCodeException("Rack code already exists: " + request.getRackCode());
        }
        PharmacyRack rack = new PharmacyRack();
        rack.setRackCode(request.getRackCode().trim());
        rack.setRackName(request.getRackName().trim());
        rack.setLocationArea(request.getLocationArea());
        rack.setStorageType(request.getStorageType());
        rack.setCategoryType(request.getCategoryType());
        rack.setLasaSafe(request.getLasaSafe() != null ? request.getLasaSafe() : Boolean.FALSE);
        rack.setMaxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 100);
        rack.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        rack.setCreatedByUser(performedBy);
        rack = rackRepository.save(rack);
        log.info("Rack created: {} by {}", rack.getRackCode(), performedBy);
        return toRackDto(rack, false);
    }

    @Transactional(readOnly = true)
    public List<RackResponseDto> listRacks(boolean includeInactive) {
        List<PharmacyRack> racks = includeInactive
                ? rackRepository.findAll()
                : rackRepository.findAllByActiveTrueOrderByRackCodeAsc();
        return racks.stream()
                .map(r -> toRackDto(r, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public RackResponseDto updateRack(Long id, RackRequestDto request, String performedBy) {
        PharmacyRack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + id));
        if (!rack.getRackCode().equalsIgnoreCase(request.getRackCode())
                && rackRepository.existsByRackCodeIgnoreCase(request.getRackCode())) {
            throw new DuplicateRackCodeException("Rack code already exists: " + request.getRackCode());
        }
        rack.setRackCode(request.getRackCode().trim());
        rack.setRackName(request.getRackName().trim());
        rack.setLocationArea(request.getLocationArea());
        rack.setStorageType(request.getStorageType());
        rack.setCategoryType(request.getCategoryType());
        rack.setLasaSafe(request.getLasaSafe() != null ? request.getLasaSafe() : Boolean.FALSE);
        rack.setMaxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 100);
        rack.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        rack = rackRepository.save(rack);
        log.info("Rack updated: {} by {}", rack.getRackCode(), performedBy);
        return toRackDto(rack, true);
    }

    @Transactional
    public void softDeleteRack(Long id, String performedBy) {
        PharmacyRack rack = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + id));
        rack.setActive(false);
        rackRepository.save(rack);
        log.info("Rack deactivated: {} by {}", rack.getRackCode(), performedBy);
    }

    @Transactional
    public ShelfResponseDto addShelf(Long rackId, ShelfRequestDto request, String performedBy) {
        PharmacyRack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + rackId));
        if (shelfRepository.existsByRack_IdAndShelfCodeIgnoreCase(rackId, request.getShelfCode())) {
            throw new IllegalArgumentException("Shelf code already exists in this rack: " + request.getShelfCode());
        }
        PharmacyShelf shelf = new PharmacyShelf();
        shelf.setRack(rack);
        shelf.setShelfCode(request.getShelfCode().trim());
        shelf.setShelfLevel(request.getShelfLevel());
        shelf.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        shelf.setBinNumber(request.getBinNumber() != null ? request.getBinNumber().trim() : null);
        shelf = shelfRepository.save(shelf);
        log.info("Shelf added: {} to rack {} by {}", shelf.getShelfCode(), rack.getRackCode(), performedBy);
        return toShelfDto(shelf);
    }

    @Transactional(readOnly = true)
    public List<ShelfResponseDto> listShelves(Long rackId) {
        return shelfRepository.findByRack_IdOrderByShelfLevelAsc(rackId).stream()
                .map(this::toShelfDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RackInventoryDto getRackInventory(Long rackId) {
        PharmacyRack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found: " + rackId));
        List<MedicineMaster> medicines = medicineRepository.findActiveByRackId(rackId);

        RackInventoryDto dto = new RackInventoryDto();
        dto.setRackId(rack.getId());
        dto.setRackCode(rack.getRackCode());
        dto.setRackName(rack.getRackName());
        dto.setLocationArea(rack.getLocationArea().name());
        dto.setStorageType(rack.getStorageType().name());

        List<RackInventoryItemDto> items = new ArrayList<>();
        for (MedicineMaster m : medicines) {
            RackInventoryItemDto item = new RackInventoryItemDto();
            item.setMedicineId(m.getId());
            item.setMedicineCode(m.getMedicineCode());
            item.setMedicineName(m.getMedicineName());
            item.setStorageType(m.getStorageType());
            item.setLasa(Boolean.TRUE.equals(m.getLasaFlag()));
            item.setShelfCode(m.getShelf() != null ? m.getShelf().getShelfCode() : null);
            item.setShelfLevel(m.getShelf() != null ? m.getShelf().getShelfLevel() : null);
            item.setBinNumber(m.getBinNumber());
            item.setBatchCount(0); // Placeholder until physical stock is implemented
            item.setNearestExpiry(null);
            item.setExpiryRiskClass("text-muted");
            items.add(item);
        }
        dto.setItems(items);
        return dto;
    }

    private RackResponseDto toRackDto(PharmacyRack r, boolean includeShelves) {
        RackResponseDto dto = new RackResponseDto();
        dto.setId(r.getId());
        dto.setRackCode(r.getRackCode());
        dto.setRackName(r.getRackName());
        dto.setLocationArea(r.getLocationArea());
        dto.setStorageType(r.getStorageType());
        dto.setCategoryType(r.getCategoryType());
        dto.setLasaSafe(r.getLasaSafe());
        dto.setMaxCapacity(r.getMaxCapacity());
        dto.setActive(r.getActive());
        dto.setCreatedByUser(r.getCreatedByUser());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        if (includeShelves) {
            dto.setShelves(shelfRepository.findByRack_IdOrderByShelfLevelAsc(r.getId()).stream()
                    .map(this::toShelfDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ShelfResponseDto toShelfDto(PharmacyShelf s) {
        ShelfResponseDto dto = new ShelfResponseDto();
        dto.setId(s.getId());
        dto.setRackId(s.getRack().getId());
        dto.setShelfCode(s.getShelfCode());
        dto.setShelfLevel(s.getShelfLevel());
        dto.setActive(s.getActive());
        dto.setBinNumber(s.getBinNumber());
        return dto;
    }
}
