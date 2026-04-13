package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.entity.PharmacyInvoice;
import com.hospital.hms.pharmacy.entity.SaleType;
import com.hospital.hms.pharmacy.entity.StockTransaction;
import com.hospital.hms.pharmacy.repository.PharmacyInvoiceRepository;
import com.hospital.hms.pharmacy.repository.StockTransactionRepository;
import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.tenant.service.TenantContextService;
import com.hospital.hms.reception.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pharmacy invoice generation. Calls Python reportlab service.
 * Does not rollback sale if PDF fails.
 */
@Service
public class PharmacyInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(PharmacyInvoiceService.class);
    private static final Pattern INV_PATTERN = Pattern.compile("^INV-(\\d{4})-(\\d+)$");

    private final PharmacyInvoiceRepository invoiceRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final TenantContextService tenantContextService;

    @Value("${pharmacy.invoice.storage-path:./data/invoices}")
    private String storagePath;

    @Value("${pharmacy.invoice.python-path:python}")
    private String pythonPath;

    @Value("${pharmacy.invoice.script-path:invoice-generator/invoice_service.py}")
    private String scriptPath;

    @Value("${pharmacy.hospital.name:Hospital Name}")
    private String hospitalName;

    @Value("${pharmacy.hospital.address:Address}")
    private String hospitalAddress;

    @Value("${pharmacy.hospital.phone:—}")
    private String hospitalPhone;

    @Value("${pharmacy.hospital.gst-no:}")
    private String hospitalGstNo;

    @Value("${pharmacy.invoice.gst-enabled:false}")
    private boolean gstEnabled;

    @Value("${pharmacy.invoice.gst-percent:0}")
    private double gstPercent;

    public PharmacyInvoiceService(PharmacyInvoiceRepository invoiceRepository,
                                  StockTransactionRepository stockTransactionRepository,
                                  TenantContextService tenantContextService) {
        this.invoiceRepository = invoiceRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.tenantContextService = tenantContextService;
    }

    /**
     * Generate invoice after successful sale(s). Supports multi-line. Does not throw - logs on failure.
     */
    @Transactional
    public Optional<PharmacyInvoice> generateAfterSale(List<StockTransaction> sales, String performedBy) {
        if (sales == null || sales.isEmpty()) return Optional.empty();
        String invoiceNumber = nextInvoiceNumber();
        Map<String, Object> data = buildInvoiceData(sales, invoiceNumber);
        Path script = Paths.get(scriptPath).toAbsolutePath();
        if (!Files.exists(script)) {
            log.warn("Invoice script not found: {}. Skipping PDF generation.", script);
            return Optional.empty();
        }
        try {
            Path tempFile = Files.createTempFile("pharmacy-inv-", ".json");
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                        .findAndRegisterModules();
                mapper.writeValue(tempFile.toFile(), data);

                ProcessBuilder pb = new ProcessBuilder(
                        pythonPath,
                        script.toString(),
                        invoiceNumber,
                        tempFile.toString()
                );
                pb.environment().put("INVOICE_STORAGE_PATH", storagePath);
                Process p = pb.start();
                int exit = p.waitFor();
                if (exit != 0) {
                    String err = new String(p.getErrorStream().readAllBytes());
                    log.error("Invoice PDF generation failed: exit={} err={}", exit, err);
                    return Optional.empty();
                }
            } finally {
                Files.deleteIfExists(tempFile);
            }

            Path pdfPath = Paths.get(storagePath).resolve(invoiceNumber + ".pdf");
            PharmacyInvoice inv = new PharmacyInvoice();
            inv.setInvoiceNumber(invoiceNumber);
            inv.setSale(sales.get(0));
            inv.setPdfPath(pdfPath.toAbsolutePath().toString());
            inv.setGeneratedBy(performedBy);
            inv.setGeneratedAt(java.time.Instant.now());
            inv = invoiceRepository.save(inv);

            try {
                MDC.put(MdcKeys.MODULE, "PHARMACY");
                log.info("PHARMACY_AUDIT invoice_generated invoiceNumber={} saleIds={} performedBy={} correlationId={}",
                        invoiceNumber, sales.stream().map(s -> s.getId().toString()).reduce((a, b) -> a + "," + b).orElse(""), performedBy, MDC.get(MdcKeys.CORRELATION_ID));
            } finally {
                MDC.remove(MdcKeys.MODULE);
            }
            return Optional.of(inv);
        } catch (Exception e) {
            log.error("Invoice PDF generation failed for sales {}: {}", sales.stream().map(s -> s.getId().toString()).reduce((a, b) -> a + "," + b).orElse(""), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Resource getInvoicePdf(String invoiceNumber) {
        PharmacyInvoice inv = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceNumber));
        // Validate invoice belongs to current hospital
        if (inv.getSale() != null && inv.getSale().getPatient() != null) {
            Long hospitalId = tenantContextService.requireCurrentHospitalId();
            if (!inv.getSale().getPatient().getHospital().getId().equals(hospitalId)) {
                throw new OperationNotAllowedException("Invoice does not belong to current hospital");
            }
        }
        Path path = Paths.get(inv.getPdfPath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Invoice PDF file not found: " + invoiceNumber);
        }
        return new FileSystemResource(path);
    }

    @Transactional
    public PharmacyInvoice regenerateForSale(Long saleId, String performedBy) {
        StockTransaction sale = stockTransactionRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + saleId));
        if (sale.getTransactionType() != com.hospital.hms.pharmacy.entity.StockTransactionType.SELL) {
            throw new IllegalArgumentException("Only sell transactions can have invoices");
        }
        invoiceRepository.findBySale_Id(saleId).ifPresent(invoiceRepository::delete);
        return generateAfterSale(List.of(sale), performedBy)
                .orElseThrow(() -> new RuntimeException("Failed to regenerate invoice PDF"));
    }

    private String nextInvoiceNumber() {
        int year = Year.now().getValue();
        String prefix = "INV-" + year + "-";
        Optional<String> maxOpt = invoiceRepository.findMaxInvoiceNumberByPrefix(prefix);
        int next = 1;
        if (maxOpt.isPresent()) {
            Matcher m = INV_PATTERN.matcher(maxOpt.get());
            if (m.matches()) {
                next = Integer.parseInt(m.group(2)) + 1;
            }
        }
        return prefix + String.format("%04d", next);
    }

    private Map<String, Object> buildInvoiceData(List<StockTransaction> sales, String invoiceNumber) {
        StockTransaction first = sales.get(0);
        Map<String, Object> hospital = new HashMap<>();
        hospital.put("name", hospitalName);
        hospital.put("address", hospitalAddress);
        hospital.put("phone", hospitalPhone);
        hospital.put("gstNo", hospitalGstNo);

        Map<String, Object> sale = new HashMap<>();
        sale.put("transactionDate", first.getTransactionDate() != null ? first.getTransactionDate().toString() : null);
        sale.put("performedBy", first.getPerformedBy());
        sale.put("saleType", first.getSaleType() != null ? first.getSaleType().name() : "MANUAL");
        sale.put("lineItems", sales.stream().map(t -> {
            Map<String, Object> line = new HashMap<>();
            line.put("medicineName", t.getMedicine().getMedicineName());
            line.put("medicineCode", t.getMedicine().getMedicineCode());
            line.put("batchNumber", t.getBatchNumber() != null ? t.getBatchNumber() : "N/A");
            line.put("expiryDate", t.getExpiryDate() != null ? t.getExpiryDate().toString() : null);
            line.put("quantity", t.getQuantity());
            BigDecimal rate = t.getCostPerUnit();
            double r = rate != null ? rate.doubleValue() : 0;
            line.put("rate", r);
            line.put("amount", r * t.getQuantity());
            return line;
        }).toList());

        Map<String, Object> patient = null;
        if (first.getSaleType() == SaleType.PATIENT && first.getPatient() != null) {
            Patient p = first.getPatient();
            patient = new HashMap<>();
            patient.put("patientName", p.getFullName());
            patient.put("uhid", p.getUhid());
            patient.put("phone", p.getPhone());
            patient.put("ipdNo", first.getReference());
            patient.put("wardBed", null);
            patient.put("ipdLinked", first.getReference() != null && first.getReference().startsWith("IPD"));
        } else if (first.getSaleType() == SaleType.MANUAL) {
            patient = new HashMap<>();
            patient.put("customerName", first.getManualPatientName());
            patient.put("phone", first.getManualPhone());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("hospital", hospital);
        data.put("sale", sale);
        data.put("patient", patient);
        data.put("gstEnabled", gstEnabled);
        data.put("gstPercent", gstPercent);

        return data;
    }
}
