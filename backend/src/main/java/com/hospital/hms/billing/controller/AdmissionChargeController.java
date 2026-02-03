package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.AdmissionChargeRequestDto;
import com.hospital.hms.billing.dto.AdmissionChargeResponseDto;
import com.hospital.hms.billing.entity.AdmissionCharge;
import com.hospital.hms.billing.entity.ChargeType;
import com.hospital.hms.billing.service.AdmissionChargeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * POST /api/billing/admissions/{admissionId}/charges — add charge (Pharmacy, Lab, Doctor Orders auto-add here).
 * GET /api/billing/admissions/{admissionId}/charges — list charges for an admission.
 */
@RestController
@RequestMapping("/billing/admissions")
public class AdmissionChargeController {

    private final AdmissionChargeService chargeService;

    public AdmissionChargeController(AdmissionChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @PostMapping("/{admissionId}/charges")
    public ResponseEntity<AdmissionChargeResponseDto> addCharge(
            @PathVariable Long admissionId,
            @Valid @RequestBody AdmissionChargeRequestDto request) {
        AdmissionCharge charge = chargeService.addCharge(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(charge));
    }

    @GetMapping("/{admissionId}/charges")
    public ResponseEntity<List<AdmissionChargeResponseDto>> listCharges(@PathVariable Long admissionId) {
        List<AdmissionChargeResponseDto> list = chargeService.findByIpdAdmissionId(admissionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private AdmissionChargeResponseDto toDto(AdmissionCharge c) {
        AdmissionChargeResponseDto dto = new AdmissionChargeResponseDto();
        dto.setId(c.getId());
        dto.setIpdAdmissionId(c.getIpdAdmission().getId());
        dto.setAdmissionNumber(c.getIpdAdmission().getAdmissionNumber());
        dto.setChargeType(c.getChargeType());
        dto.setAmount(c.getAmount());
        dto.setDescription(c.getDescription());
        dto.setReferenceType(c.getReferenceType());
        dto.setReferenceId(c.getReferenceId());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
