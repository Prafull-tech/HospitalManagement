package com.hospital.hms.pharmacy.controller;

import com.hospital.hms.pharmacy.dto.BarcodeEntryRequestDto;
import com.hospital.hms.pharmacy.dto.BatchSuggestionDto;
import com.hospital.hms.pharmacy.dto.ExistingMedicineBatchRequestDto;
import com.hospital.hms.pharmacy.dto.ExpiryAlertDto;
import com.hospital.hms.pharmacy.dto.FefoStockRowDto;
import com.hospital.hms.pharmacy.dto.IpdIssueQueueItemDto;
import com.hospital.hms.pharmacy.dto.IssueQueuePatientDto;
import com.hospital.hms.pharmacy.dto.ManualEntryRequestDto;
import com.hospital.hms.pharmacy.dto.MedicationIssueRequestDto;
import com.hospital.hms.pharmacy.dto.MedicationOrderRequestDto;
import com.hospital.hms.pharmacy.dto.MedicineImportResultDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterRequestDto;
import com.hospital.hms.pharmacy.dto.MedicineLookupResponseDto;
import com.hospital.hms.pharmacy.dto.MedicineMasterResponseDto;
import com.hospital.hms.pharmacy.dto.PharmacySummaryDto;
import com.hospital.hms.pharmacy.dto.PatientIpdStatusDto;
import com.hospital.hms.pharmacy.dto.PharmacySellResponseDto;
import com.hospital.hms.pharmacy.dto.PurchaseRequestDto;
import com.hospital.hms.pharmacy.dto.SellRequestDto;
import com.hospital.hms.pharmacy.dto.StockTransactionResponseDto;
import com.hospital.hms.pharmacy.service.MedicationOrderService;
import com.hospital.hms.pharmacy.service.MedicineImportService;
import com.hospital.hms.pharmacy.service.MedicineLookupService;
import com.hospital.hms.pharmacy.service.MedicineMasterService;
import com.hospital.hms.pharmacy.service.PharmacyInvoiceService;
import com.hospital.hms.pharmacy.service.PharmacyPatientService;
import com.hospital.hms.pharmacy.service.PharmacySimulationService;
import com.hospital.hms.pharmacy.service.StockTransactionService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

/**
 * Temporary pharmacy REST API for driving the Pharmacy Dashboard UI.
 * Returns simulated data only – uses MySQL.
 */
@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final PharmacySimulationService pharmacyService;
    private final MedicationOrderService medicationOrderService;
    private final MedicineMasterService medicineService;
    private final MedicineLookupService medicineLookupService;
    private final MedicineImportService medicineImportService;
    private final StockTransactionService stockTransactionService;
    private final PharmacyPatientService pharmacyPatientService;
    private final PharmacyInvoiceService pharmacyInvoiceService;

    public PharmacyController(PharmacySimulationService pharmacyService,
                              MedicationOrderService medicationOrderService,
                              MedicineMasterService medicineService,
                              MedicineLookupService medicineLookupService,
                              MedicineImportService medicineImportService,
                              StockTransactionService stockTransactionService,
                              PharmacyPatientService pharmacyPatientService,
                              PharmacyInvoiceService pharmacyInvoiceService) {
        this.pharmacyService = pharmacyService;
        this.medicationOrderService = medicationOrderService;
        this.medicineService = medicineService;
        this.medicineLookupService = medicineLookupService;
        this.medicineImportService = medicineImportService;
        this.stockTransactionService = stockTransactionService;
        this.pharmacyPatientService = pharmacyPatientService;
        this.pharmacyInvoiceService = pharmacyInvoiceService;
    }

    @GetMapping("/ipd/issue-queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<IpdIssueQueueItemDto> getIssueQueue(@RequestParam(required = false) String q) {
        return pharmacyService.getIssueQueue(q);
    }

    @GetMapping("/issue-queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<IssueQueuePatientDto> getMedicationIssueQueue(@RequestParam(required = false) String q) {
        return medicationOrderService.getIssueQueue(q);
    }

    @GetMapping("/pending/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<com.hospital.hms.ipd.dto.DischargePendingItemDto> getPendingByIpd(@PathVariable Long ipdAdmissionId) {
        return medicationOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
    }

    @GetMapping("/batch/suggest/{medicineId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<BatchSuggestionDto> getBatchSuggestion(@PathVariable Long medicineId) {
        BatchSuggestionDto dto = medicationOrderService.getBatchSuggestion(medicineId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<Void> issueMedications(@Valid @RequestBody MedicationIssueRequestDto request) {
        medicationOrderService.issueOrders(request, StockTransactionService.resolvePerformedBy());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/medication-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACY_MANAGER', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<java.util.Map<String, Object>> createMedicationOrder(@Valid @RequestBody MedicationOrderRequestDto request) {
        var order = medicationOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of("id", order.getId(), "status", order.getStatus().name()));
    }

    @PostMapping("/ipd/indents/{indentId}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<Void> issueIndent(@PathVariable Long indentId) {
        pharmacyService.issueIndent(indentId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ipd/indents/{indentId}/issue-partial")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<Void> issueIndentPartial(@PathVariable Long indentId) {
        pharmacyService.issueIndent(indentId, true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stock/fefo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public List<FefoStockRowDto> getFefoStock(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String risk) {
        return pharmacyService.getFefoStock(q, risk);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public List<ExpiryAlertDto> getAlerts() {
        return pharmacyService.getAlerts();
    }

    @PostMapping("/alerts/{id}/ack")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ExpiryAlertDto acknowledge(@PathVariable Long id) {
        return pharmacyService.acknowledgeAlert(id);
    }

    @GetMapping("/summary/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public PharmacySummaryDto getTodaySummary() {
        return pharmacyService.getTodaySummary();
    }

    @GetMapping(value = "/summary/today/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
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
        MedicineMasterResponseDto dto = medicineService.create(request, StockTransactionService.resolvePerformedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/medicines/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineMasterResponseDto> getMedicineByBarcode(@PathVariable String barcode) {
        return medicineService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medicines/lookup/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public ResponseEntity<MedicineLookupResponseDto> lookupMedicine(@PathVariable String barcode) {
        return medicineLookupService.lookup(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/medicines/manual")
    @PreAuthorize("hasAnyRole('PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineMasterResponseDto> createManual(
            @Valid @RequestBody ManualEntryRequestDto request) {
        MedicineMasterResponseDto dto = medicineService.createManual(request, StockTransactionService.resolvePerformedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/medicines/barcode")
    @PreAuthorize("hasAnyRole('PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineMasterResponseDto> createFromBarcode(
            @Valid @RequestBody BarcodeEntryRequestDto request) {
        MedicineMasterResponseDto dto = medicineService.createFromBarcode(request, StockTransactionService.resolvePerformedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/medicines/existing")
    @PreAuthorize("hasAnyRole('PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineMasterResponseDto> addBatchToExisting(
            @Valid @RequestBody ExistingMedicineBatchRequestDto request) {
        MedicineMasterResponseDto dto = medicineService.addBatchToExisting(request, StockTransactionService.resolvePerformedBy());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/medicines")
    @PreAuthorize("permitAll()")
    public List<MedicineMasterResponseDto> listMedicines(@RequestParam(required = false) String q) {
        return (q != null && !q.isBlank())
                ? medicineService.search(q)
                : medicineService.listAll();
    }

    @PutMapping("/medicines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public MedicineMasterResponseDto updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineMasterRequestDto request) {
        return medicineService.update(id, request, "PHARMACY_USER");
    }

    @DeleteMapping("/medicines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<Void> softDeleteMedicine(@PathVariable Long id) {
        medicineService.softDelete(id, "PHARMACY_USER");
        return ResponseEntity.noContent().build();
    }

    // ---------- Medicine Import (Excel) ----------

    @GetMapping(value = "/medicines/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<byte[]> downloadMedicineTemplate(
            @RequestParam(defaultValue = "master") String type) {
        try {
            byte[] template = "stock".equalsIgnoreCase(type)
                    ? medicineImportService.generateStockTemplate()
                    : medicineImportService.generateMasterTemplate();
            String filename = "stock".equalsIgnoreCase(type)
                    ? "stock-import-template.xlsx"
                    : "medicine-master-import-template.xlsx";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .body(template);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    @PostMapping(value = "/medicines/import/master", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineImportResultDto> importMedicineMaster(@RequestParam("file") MultipartFile file) {
        try {
            MedicineImportResultDto result = medicineImportService.importMasterFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/medicines/import/stock", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineImportResultDto> importStock(@RequestParam("file") MultipartFile file) {
        try {
            MedicineImportResultDto result = medicineImportService.importStockFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/medicines/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<MedicineImportResultDto> importMedicines(@RequestParam("file") MultipartFile file) {
        try {
            MedicineImportResultDto result = medicineImportService.importMasterFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    @GetMapping("/patients/{patientId}/ipd-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public PatientIpdStatusDto getPatientIpdStatus(@PathVariable Long patientId) {
        return pharmacyPatientService.getActiveIpdStatus(patientId);
    }

    // ---------- Purchase & Sell (Stock In/Out) ----------

    @PostMapping("/stock/purchase")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<StockTransactionResponseDto> purchase(
            @Valid @RequestBody PurchaseRequestDto request) {
        String performedBy = StockTransactionService.resolvePerformedBy();
        StockTransactionResponseDto dto = stockTransactionService.purchase(request, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/stock/sell")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'BILLING')")
    public ResponseEntity<PharmacySellResponseDto> sell(
            @Valid @RequestBody SellRequestDto request) {
        String performedBy = StockTransactionService.resolvePerformedBy();
        PharmacySellResponseDto dto = stockTransactionService.sell(request, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/stock/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST')")
    public List<StockTransactionResponseDto> listTransactions(
            @RequestParam(required = false) Long medicineId,
            @RequestParam(defaultValue = "50") int limit) {
        if (medicineId != null) {
            return stockTransactionService.listByMedicine(medicineId, limit);
        }
        return stockTransactionService.listRecent(limit);
    }

    // ---------- Pharmacy Invoice PDF ----------

    @GetMapping(value = "/invoice/{invoiceNumber}", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'BILLING')")
    public ResponseEntity<Resource> getInvoicePdf(@PathVariable String invoiceNumber) {
        Resource resource = pharmacyInvoiceService.getInvoicePdf(invoiceNumber);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + invoiceNumber + ".pdf\"")
                .body(resource);
    }

    @PostMapping("/invoice/{saleId}/regenerate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST', 'PHARMACIST', 'BILLING')")
    public ResponseEntity<PharmacySellResponseDto> regenerateInvoice(@PathVariable Long saleId) {
        String performedBy = StockTransactionService.resolvePerformedBy();
        var inv = pharmacyInvoiceService.regenerateForSale(saleId, performedBy);
        PharmacySellResponseDto dto = new PharmacySellResponseDto();
        dto.setSuccess(true);
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setPdfUrl("/pharmacy/invoice/" + inv.getInvoiceNumber());
        return ResponseEntity.ok(dto);
    }

    // ---------- Medicine Import (Excel) ----------

    @PostMapping(value = "/medicines/import/error-report", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<byte[]> downloadImportErrorReport(@RequestBody MedicineImportResultDto result) {
        if (result.getErrors() == null || result.getErrors().isEmpty()) {
            throw new IllegalArgumentException("No errors to report");
        }
        try {
            byte[] report = medicineImportService.generateErrorReport(result.getErrors());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=medicine-import-errors.xlsx")
                    .body(report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate error report", e);
        }
    }
}

