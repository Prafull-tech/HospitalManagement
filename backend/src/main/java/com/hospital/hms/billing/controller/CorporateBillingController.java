package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.CorporateInvoiceRequestDto;
import com.hospital.hms.billing.service.CorporateBillingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Corporate billing API. Generate corporate invoice, approve for discharge.
 */
@RestController
@RequestMapping("/billing/corporate")
public class CorporateBillingController {

    private final CorporateBillingService corporateBillingService;

    public CorporateBillingController(CorporateBillingService corporateBillingService) {
        this.corporateBillingService = corporateBillingService;
    }

    @PostMapping("/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<BillingAccountViewDto> createCorporateInvoice(@Valid @RequestBody CorporateInvoiceRequestDto request) {
        return ResponseEntity.ok(corporateBillingService.createCorporateInvoice(request));
    }
}
