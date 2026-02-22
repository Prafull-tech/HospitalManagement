package com.hospital.hms.opd.controller;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.opd.dto.OpdGroupBillingRequestDto;
import com.hospital.hms.opd.service.OpdGroupBillingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/opd/billing")
public class OpdGroupBillingController {

    private final OpdGroupBillingService opdGroupBillingService;

    public OpdGroupBillingController(OpdGroupBillingService opdGroupBillingService) {
        this.opdGroupBillingService = opdGroupBillingService;
    }

    @PostMapping("/group")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'RECEPTIONIST')")
    public ResponseEntity<BillingAccountViewDto> createGroupBill(@Valid @RequestBody OpdGroupBillingRequestDto request) {
        return ResponseEntity.ok(opdGroupBillingService.createGroupBill(request));
    }
}
