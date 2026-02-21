package com.hospital.hms.pharmacy.controller;

import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.dto.RackInventoryDto;
import com.hospital.hms.pharmacy.dto.RackRequestDto;
import com.hospital.hms.pharmacy.dto.RackResponseDto;
import com.hospital.hms.pharmacy.dto.RackSuggestionResponseDto;
import com.hospital.hms.pharmacy.dto.ShelfRequestDto;
import com.hospital.hms.pharmacy.dto.ShelfResponseDto;
import com.hospital.hms.pharmacy.service.PharmacyRackService;
import com.hospital.hms.pharmacy.service.RackSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Rack and shelf management API.
 * PHARMACY_MANAGER, STORE_INCHARGE: full access.
 * IPD_PHARMACIST, PHARMACIST: view only (racks, inventory).
 */
@RestController
@RequestMapping("/pharmacy/racks")
public class PharmacyRackController {

    private final PharmacyRackService rackService;
    private final RackSuggestionService suggestionService;

    public PharmacyRackController(PharmacyRackService rackService, RackSuggestionService suggestionService) {
        this.rackService = rackService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/suggest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<RackSuggestionResponseDto> suggestRack(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String storageType,
            @RequestParam(defaultValue = "false") boolean lasaFlag) {
        Optional<RackSuggestionResponseDto> result = suggestionService.suggest(
                category,
                storageType != null ? storageType : "ROOM_TEMP",
                lasaFlag);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<RackResponseDto> createRack(@Valid @RequestBody RackRequestDto request) {
        RackResponseDto dto = rackService.createRack(request, SecurityContextUserResolver.resolveUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<RackResponseDto> listRacks(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return rackService.listRacks(includeInactive);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public RackResponseDto updateRack(@PathVariable Long id, @Valid @RequestBody RackRequestDto request) {
        return rackService.updateRack(id, request, SecurityContextUserResolver.resolveUserId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<Void> softDeleteRack(@PathVariable Long id) {
        rackService.softDeleteRack(id, SecurityContextUserResolver.resolveUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rackId}/shelves")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<ShelfResponseDto> addShelf(@PathVariable Long rackId,
                                                      @Valid @RequestBody ShelfRequestDto request) {
        ShelfResponseDto dto = rackService.addShelf(rackId, request, SecurityContextUserResolver.resolveUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{rackId}/shelves")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<ShelfResponseDto> listShelves(@PathVariable Long rackId) {
        return rackService.listShelves(rackId);
    }

    @GetMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public RackInventoryDto getRackInventory(@PathVariable Long id) {
        return rackService.getRackInventory(id);
    }
}
