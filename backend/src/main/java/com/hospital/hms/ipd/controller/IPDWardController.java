package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.dto.BedAvailabilityResponseDto;
import com.hospital.hms.ipd.dto.WardResponseDto;
import com.hospital.hms.ipd.entity.BedAllocation;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ward.dto.BedResponseDto;
import com.hospital.hms.ward.dto.BedStatusRequestDto;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.service.BedService;
import com.hospital.hms.ward.service.WardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST API for IPD wards and bed availability. Delegates to ward module. Base path: /api (context) + /ipd (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/ipd")
public class IPDWardController {

    private final WardService wardService;
    private final BedService bedService;
    private final BedAllocationRepository bedAllocationRepository;

    public IPDWardController(WardService wardService, BedService bedService,
                             BedAllocationRepository bedAllocationRepository) {
        this.wardService = wardService;
        this.bedService = bedService;
        this.bedAllocationRepository = bedAllocationRepository;
    }

    @GetMapping("/wards")
    public ResponseEntity<List<WardResponseDto>> listWards(
            @RequestParam(required = false) WardType wardType) {
        List<com.hospital.hms.ward.dto.WardResponseDto> list = wardService.list(true, wardType);
        List<WardResponseDto> ipdList = list.stream().map(this::toIpdWardDto).collect(Collectors.toList());
        return ResponseEntity.ok(ipdList);
    }

    /**
     * Hospital bed availability for IPD admission (read-only). Selection only; no allocation here.
     * GET /api/ipd/hospital-beds?hospitalId=1&wardType=GENERAL&vacantOnly=true
     * Ward types: GENERAL, SEMI_PRIVATE, PRIVATE, ICU, EMERGENCY. Bed statuses: VACANT, OCCUPIED, RESERVED, CLEANING.
     * Only beds with status VACANT (AVAILABLE) are selectable for admission; use selectableForAdmission or vacantOnly.
     */
    @GetMapping("/hospital-beds")
    public ResponseEntity<List<BedAvailabilityResponseDto>> getHospitalBeds(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false, defaultValue = "false") boolean vacantOnly) {
        List<BedResponseDto> list = bedService.getAvailability(null);
        Stream<BedResponseDto> stream = list.stream();
        if (wardType != null) {
            stream = stream.filter(b -> wardType.equals(b.getWardType()));
        }
        if (vacantOnly) {
            stream = stream.filter(b -> Boolean.TRUE.equals(b.getAvailable()));
        }
        List<BedAvailabilityResponseDto> ipdList = stream
                .map(this::toIpdBedDto)
                .peek(dto -> {
                    if (BedStatus.OCCUPIED.equals(dto.getBedStatus())) {
                        bedAllocationRepository.findActiveByBedIdWithAdmissionAndPatient(dto.getBedId()).ifPresent(ba -> {
                            dto.setPatientId(ba.getAdmission().getPatient().getId());
                            dto.setPatientName(ba.getAdmission().getPatient().getFullName());
                            dto.setPatientUhid(ba.getAdmission().getPatient().getUhid());
                            dto.setAdmissionNumber(ba.getAdmission().getAdmissionNumber());
                            dto.setAdmissionId(ba.getAdmission().getId());
                        });
                    }
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ipdList);
    }

    @GetMapping("/beds/availability")
    public ResponseEntity<List<BedAvailabilityResponseDto>> getBedAvailability(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false) BedStatus bedStatus,
            @RequestParam(required = false) String search) {
        List<BedResponseDto> list = bedService.getAvailability(wardId);
        Stream<BedResponseDto> stream = list.stream();
        if (wardType != null) {
            stream = stream.filter(b -> wardType.equals(b.getWardType()));
        }
        if (bedStatus != null) {
            stream = stream.filter(b -> bedStatus.equals(b.getBedStatus()));
        }
        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase();
            stream = stream.filter(b -> b.getBedNumber() != null && b.getBedNumber().toLowerCase().contains(term));
        }
        List<BedAvailabilityResponseDto> ipdList = stream
                .map(b -> toIpdBedDto(b))
                .peek(dto -> {
                    if (BedStatus.OCCUPIED.equals(dto.getBedStatus())) {
                        bedAllocationRepository.findActiveByBedIdWithAdmissionAndPatient(dto.getBedId()).ifPresent(ba -> {
                            dto.setPatientId(ba.getAdmission().getPatient().getId());
                            dto.setPatientName(ba.getAdmission().getPatient().getFullName());
                            dto.setPatientUhid(ba.getAdmission().getPatient().getUhid());
                            dto.setAdmissionNumber(ba.getAdmission().getAdmissionNumber());
                            dto.setAdmissionId(ba.getAdmission().getId());
                        });
                    }
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ipdList);
    }

    @PutMapping("/beds/{bedId}/status")
    public ResponseEntity<BedAvailabilityResponseDto> updateBedStatus(@PathVariable Long bedId,
                                                                       @Valid @RequestBody BedStatusRequestDto request) {
        BedResponseDto updated = bedService.updateStatus(bedId, request);
        BedAvailabilityResponseDto dto = toIpdBedDto(updated);
        if (BedStatus.OCCUPIED.equals(dto.getBedStatus())) {
            bedAllocationRepository.findActiveByBedIdWithAdmissionAndPatient(bedId).ifPresent(ba -> {
                dto.setPatientId(ba.getAdmission().getPatient().getId());
                dto.setPatientName(ba.getAdmission().getPatient().getFullName());
                dto.setPatientUhid(ba.getAdmission().getPatient().getUhid());
                dto.setAdmissionNumber(ba.getAdmission().getAdmissionNumber());
                dto.setAdmissionId(ba.getAdmission().getId());
            });
        }
        return ResponseEntity.ok(dto);
    }

    private WardResponseDto toIpdWardDto(com.hospital.hms.ward.dto.WardResponseDto w) {
        WardResponseDto dto = new WardResponseDto();
        dto.setId(w.getId());
        dto.setCode(w.getCode());
        dto.setName(w.getName());
        dto.setWardType(w.getWardType());
        dto.setCapacity(w.getCapacity());
        dto.setIsActive(w.getIsActive());
        return dto;
    }

    private BedAvailabilityResponseDto toIpdBedDto(BedResponseDto b) {
        BedAvailabilityResponseDto dto = new BedAvailabilityResponseDto();
        dto.setBedId(b.getId());
        dto.setBedNumber(b.getBedNumber());
        dto.setWardId(b.getWardId());
        dto.setWardName(b.getWardName());
        dto.setWardCode(b.getWardCode());
        dto.setWardType(b.getWardType());
        dto.setRoomId(b.getRoomId());
        dto.setRoomNumber(b.getRoomNumber());
        dto.setBedStatus(b.getBedStatus());
        dto.setBedStatusDisplay(toBedStatusDisplay(b.getBedStatus()));
        dto.setAvailable(Boolean.TRUE.equals(b.getAvailable()));
        dto.setSelectableForAdmission(Boolean.TRUE.equals(b.getAvailable()));
        dto.setUpdatedAt(b.getUpdatedAt());
        return dto;
    }

    /** Maps BedStatus to display label (VACANT for AVAILABLE). */
    private static String toBedStatusDisplay(BedStatus status) {
        if (status == null) return null;
        if (status == BedStatus.AVAILABLE) return "VACANT";
        return status.name();
    }
}
