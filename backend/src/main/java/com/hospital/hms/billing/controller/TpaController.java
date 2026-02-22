package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.TpaPreauthRequestDto;
import com.hospital.hms.billing.dto.TpaPreauthResponseDto;
import com.hospital.hms.billing.service.TpaService;
import jakarta.validation.Valid;
import org.slf4.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * TPA / Insurance pre-authorization API.
 * POST /api/billing/tpa/preauth — submit pre-auth request.
 */
@RestController
@RequestMapping("/billing/tpa")
public class TpaController {

    private final TpaService tpaService;

    public TpaController(TpaService tpaService) {
        this.tpaService = tpaService;
    }

    @PostMapping("/preauth")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<TpaPreauthResponseDto> preauth(@Valid @RequestBody TpaPreauthRequestDto request) {
        TpaPreauthResponseDto response = tpaService.submitPreauth(request);
        return ResponseEntity.ok(response);
    }
}
