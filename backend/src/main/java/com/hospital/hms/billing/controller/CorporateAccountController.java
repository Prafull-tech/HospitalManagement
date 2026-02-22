package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.entity.CorporateAccount;
import com.hospital.hms.billing.repository.CorporateAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing/corporate-accounts")
public class CorporateAccountController {

    private final CorporateAccountRepository corporateAccountRepository;

    public CorporateAccountController(CorporateAccountRepository corporateAccountRepository) {
        this.corporateAccountRepository = corporateAccountRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<List<CorporateAccount>> list() {
        return ResponseEntity.ok(corporateAccountRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorporateAccount> create(@Valid @RequestBody CorporateAccount account) {
        return ResponseEntity.ok(corporateAccountRepository.save(account));
    }
}
