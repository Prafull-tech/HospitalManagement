package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.EMICreateRequestDto;
import com.hospital.hms.billing.service.EMIPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/emi")
public class EMIPlanController {

    private final EMIPlanService emiPlanService;

    public EMIPlanController(EMIPlanService emiPlanService) {
        this.emiPlanService = emiPlanService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<BillingAccountViewDto> createEMIPlan(@Valid @RequestBody EMICreateRequestDto request) {
        return ResponseEntity.ok(emiPlanService.createEMIPlan(request));
    }
}
