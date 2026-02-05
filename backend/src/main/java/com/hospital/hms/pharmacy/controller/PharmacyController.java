package com.hospital.hms.pharmacy.controller;

import com.hospital.hms.pharmacy.dto.ExpiryAlertDto;
import com.hospital.hms.pharmacy.dto.FefoStockRowDto;
import com.hospital.hms.pharmacy.dto.IpdIssueQueueItemDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterRequestDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterResponseDto;
import com.hospital.hms.pharmacy.dto.PharmacySummaryDto;
import com.hospital.hms.pharmacy.service.MedicineMasterService;
import com.hospital.hms.pharmacy.service.PharmacySimulationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Temporary pharmacy REST API for driving the Pharmacy Dashboard UI.
 * Returns simulated data only – safe to use on both H2 and MySQL profiles.
 */
@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final PharmacySimulationService pharmacyService;
    private final MedicineMasterService medicineService;

    public PharmacyController(PharmacySimulationService pharmacyService,
                              MedicineMasterService medicineService) {
        this.pharmacyService = pharmacyService;
        this.medicineService = medicineService;
    }

    @GetMapping("/ipd/issue-queue")
    public List<IpdIssueQueueItemDto> getIssueQueue(@RequestParam(required = false) String q) {
        return pharmacyService.getIssueQueue(q);
    }

    @PostMapping("/ipd/indents/{indentId}/issue")
    public ResponseEntity<Void> issueIndent(@PathVariable Long indentId) {
        pharmacyService.issueIndent(indentId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ipd/indents/{indentId}/issue-partial")
    public ResponseEntity<Void> issueIndentPartial(@PathVariable Long indentId) {
        pharmacyService.issueIndent(indentId, true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stock/fefo")
    public List<FefoStockRowDto> getFefoStock(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String risk) {
        return pharmacyService.getFefoStock(q, risk);
    }

    @GetMapping("/alerts")
    public List<ExpiryAlertDto> getAlerts() {
        return pharmacyService.getAlerts();
    }

    @PostMapping("/alerts/{id}/ack")
    public ExpiryAlertDto acknowledge(@PathVariable Long id) {
        return pharmacyService.acknowledgeAlert(id);
    }

    @GetMapping("/summary/today")
    public PharmacySummaryDto getTodaySummary() {
        return pharmacyService.getTodaySummary();
    }

    @GetMapping(value = "/summary/today/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportTodaySummary(@RequestParam String format) {
        // Simulation: return empty file with correct headers so frontend wiring works.
        byte[] data = new byte[0];
        String ext = format.equalsIgnoreCase("PDF") ? "pdf" : "xlsx";
        String filename = "pharmacy-summary-today." + ext;
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(data);
    }

    // ---------- Medicine Master ----------

    @PostMapping("/medicines")
    @PreAuthorize("hasAnyRole('PHARMACY_MANAGER','STORE_INCHARGE')")
    public ResponseEntity<MedicineMasterResponseDto> createMedicine(
            @Valid @RequestBody MedicineMasterRequestDto request) {
        // TODO when auth is enabled: derive performedBy from SecurityContext
        MedicineMasterResponseDto dto = medicineService.create(request, "PHARMACY_USER");
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/medicines")
    @PreAuthorize("permitAll()")
    public List<MedicineMasterResponseDto> listMedicines() {
        return medicineService.listAll();
    }

    @PutMapping("/medicines/{id}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public MedicineMasterResponseDto updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineMasterRequestDto request) {
        return medicineService.update(id, request, "PHARMACY_USER");
    }

    @DeleteMapping("/medicines/{id}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<Void> softDeleteMedicine(@PathVariable Long id) {
        medicineService.softDelete(id, "PHARMACY_USER");
        return ResponseEntity.noContent().build();
    }
}

