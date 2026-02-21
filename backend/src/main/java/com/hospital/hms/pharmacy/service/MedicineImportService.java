package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.pharmacy.dto.MedicineImportErrorDto;
import com.hospital.hms.pharmacy.dto.MedicineImportResultDto;
import com.hospital.hms.pharmacy.dto.PurchaseRequestDto;
import com.hospital.hms.pharmacy.entity.MedicineCategory;
import com.hospital.hms.pharmacy.entity.MedicineForm;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.MedicineEntryAuditLog;
import com.hospital.hms.pharmacy.entity.MedicineImportAuditLog;
import com.hospital.hms.pharmacy.entity.PharmacyRack;
import com.hospital.hms.pharmacy.entity.StorageType;
import com.hospital.hms.pharmacy.repository.MedicineEntryAuditLogRepository;
import com.hospital.hms.pharmacy.repository.MedicineImportAuditLogRepository;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.pharmacy.repository.PharmacyRackRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for medicine Excel import: Master (medicine definitions) and Stock (batch/quantity).
 * NABH audit-ready with correlation ID and audit logging.
 */
@Service
public class MedicineImportService {

    private static final Logger log = LoggerFactory.getLogger(MedicineImportService.class);

    private static final String[] MASTER_HEADERS = {
            "MedicineCode", "MedicineName", "Category", "Strength", "Form",
            "MinStock", "LASA", "StorageType", "Active"
    };

    private static final String[] STOCK_HEADERS = {
            "MedicineCode", "BatchNo", "ExpiryDate", "Quantity", "CostPrice", "Rack"
    };

    private static final int MAX_ROWS = 5000;

    private final MedicineMasterRepository medicineRepository;
    private final PharmacyRackRepository rackRepository;
    private final MedicineImportAuditLogRepository importAuditRepository;
    private final MedicineEntryAuditLogRepository entryAuditRepository;
    private final StockTransactionService stockTransactionService;

    public MedicineImportService(MedicineMasterRepository medicineRepository,
                                 PharmacyRackRepository rackRepository,
                                 MedicineImportAuditLogRepository importAuditRepository,
                                 MedicineEntryAuditLogRepository entryAuditRepository,
                                 StockTransactionService stockTransactionService) {
        this.medicineRepository = medicineRepository;
        this.rackRepository = rackRepository;
        this.importAuditRepository = importAuditRepository;
        this.entryAuditRepository = entryAuditRepository;
        this.stockTransactionService = stockTransactionService;
    }

    /**
     * Generate template for Medicine Master import (no Quantity).
     */
    public byte[] generateMasterTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Medicine Master");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < MASTER_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(MASTER_HEADERS[i]);
            }
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("AB01");
            sampleRow.createCell(1).setCellValue("Ceftriaxone");
            sampleRow.createCell(2).setCellValue("Antibiotic");
            sampleRow.createCell(3).setCellValue("1 gm");
            sampleRow.createCell(4).setCellValue("Injection");
            sampleRow.createCell(5).setCellValue(100);
            sampleRow.createCell(6).setCellValue("No");
            sampleRow.createCell(7).setCellValue("RoomTemp");
            sampleRow.createCell(8).setCellValue(true);
            for (int i = 0; i < MASTER_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generate template for Stock import (Quantity mandatory).
     */
    public byte[] generateStockTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Stock");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < STOCK_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(STOCK_HEADERS[i]);
            }
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("AB01");
            sampleRow.createCell(1).setCellValue("BATCH001");
            sampleRow.createCell(2).setCellValue("2026-12-31");
            sampleRow.createCell(3).setCellValue(100);
            sampleRow.createCell(4).setCellValue(25.50);
            sampleRow.createCell(5).setCellValue("R-ANT-01");
            for (int i = 0; i < STOCK_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** @deprecated Use generateMasterTemplate or generateStockTemplate */
    public byte[] generateTemplate() throws IOException {
        return generateMasterTemplate();
    }

    /**
     * Import Medicine Master from Excel. No Quantity column expected.
     */
    @Transactional(rollbackFor = Exception.class)
    public MedicineImportResultDto importMasterFromExcel(MultipartFile file) throws IOException {
        String correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String performedBy = SecurityContextUserResolver.resolveUserId();
        validateFile(file);
        MedicineImportResultDto result = new MedicineImportResultDto();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("Excel file has no sheets");
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("First row must contain headers");
            validateMasterHeaders(headerRow);

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum <= 0) {
                result.setTotalRows(0);
                result.setSuccessCount(0);
                result.setFailedCount(0);
                writeImportAudit(performedBy, 0, 0, 0, correlationId, file.getOriginalFilename(), "MASTER");
                return result;
            }
            if (lastRowNum > MAX_ROWS) {
                throw new IllegalArgumentException("Maximum " + MAX_ROWS + " rows allowed. File has " + lastRowNum + " data rows.");
            }

            Set<String> seenCodesInFile = new HashSet<>();
            List<MedicineMaster> toInsert = new ArrayList<>();
            List<MedicineImportErrorDto> errors = new ArrayList<>();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                int excelRowNum = r + 1;
                if (row == null || isEmptyRow(row, 5)) continue;
                result.setTotalRows(result.getTotalRows() + 1);

                MedicineMaster entity = parseAndValidateMasterRow(row, excelRowNum, seenCodesInFile, errors);
                if (entity != null) {
                    if (medicineRepository.existsByMedicineCodeIgnoreCase(entity.getMedicineCode())) {
                        errors.add(new MedicineImportErrorDto(excelRowNum, "Duplicate MedicineCode (already in database)"));
                    } else {
                        toInsert.add(entity);
                        seenCodesInFile.add(entity.getMedicineCode().toUpperCase());
                    }
                }
            }

            int inserted = 0;
            for (MedicineMaster m : toInsert) {
                m.setCreatedByUser(performedBy);
                m.setQuantity(0);
                medicineRepository.save(m);
                inserted++;
            }

            result.setSuccessCount(inserted);
            result.setFailedCount(result.getTotalRows() - inserted);
            result.setErrors(errors);
            writeImportAudit(performedBy, result.getTotalRows(), inserted, result.getFailedCount(),
                    correlationId, file.getOriginalFilename(), "MASTER");
            writeEntryAudit(performedBy, correlationId, file.getOriginalFilename());

            log.info("pharm ({}) imported {} medicine masters from Excel (correlationId={}, total={}, failed={})",
                    performedBy, inserted, correlationId, result.getTotalRows(), result.getFailedCount());
            return result;
        } catch (IOException e) {
            log.warn("Medicine master import failed (correlationId={}): {}", correlationId, e.getMessage());
            throw e;
        }
    }

    /**
     * Import Stock from Excel. Quantity is mandatory.
     */
    @Transactional(rollbackFor = Exception.class)
    public MedicineImportResultDto importStockFromExcel(MultipartFile file) throws IOException {
        String correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String performedBy = SecurityContextUserResolver.resolveUserId();
        validateFile(file);
        MedicineImportResultDto result = new MedicineImportResultDto();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("Excel file has no sheets");
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("First row must contain headers");
            validateStockHeaders(headerRow);

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum <= 0) {
                result.setTotalRows(0);
                result.setSuccessCount(0);
                result.setFailedCount(0);
                writeImportAudit(performedBy, 0, 0, 0, correlationId, file.getOriginalFilename(), "STOCK");
                return result;
            }
            if (lastRowNum > MAX_ROWS) {
                throw new IllegalArgumentException("Maximum " + MAX_ROWS + " rows allowed. File has " + lastRowNum + " data rows.");
            }

            List<MedicineImportErrorDto> errors = new ArrayList<>();
            int successCount = 0;

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                int excelRowNum = r + 1;
                if (row == null || isEmptyRow(row, 4)) continue;
                result.setTotalRows(result.getTotalRows() + 1);

                StockImportRow parsed = parseAndValidateStockRow(row, excelRowNum, errors);
                if (parsed == null) continue;

                Optional<MedicineMaster> medicineOpt = medicineRepository.findByMedicineCodeIgnoreCase(parsed.medicineCode);
                if (medicineOpt.isEmpty()) {
                    errors.add(new MedicineImportErrorDto(excelRowNum, "MedicineCode not found: " + parsed.medicineCode));
                    continue;
                }

                MedicineMaster medicine = medicineOpt.get();
                if (!Boolean.TRUE.equals(medicine.getActive())) {
                    errors.add(new MedicineImportErrorDto(excelRowNum, "Medicine is inactive: " + parsed.medicineCode));
                    continue;
                }

                try {
                    PurchaseRequestDto purchase = new PurchaseRequestDto();
                    purchase.setMedicineId(medicine.getId());
                    purchase.setQuantity(parsed.quantity);
                    purchase.setTransactionDate(LocalDate.now());
                    purchase.setBatchNumber(parsed.batchNo);
                    purchase.setExpiryDate(parsed.expiryDate);
                    purchase.setCostPerUnit(parsed.costPrice);
                    stockTransactionService.purchase(purchase, performedBy);

                    if (parsed.rackCode != null && !parsed.rackCode.isBlank()) {
                        rackRepository.findAllByActiveTrueOrderByRackCodeAsc().stream()
                                .filter(rack -> rack.getRackCode().equalsIgnoreCase(parsed.rackCode.trim()))
                                .findFirst()
                                .ifPresent(rack -> {
                                    medicine.setRack(rack);
                                    medicineRepository.save(medicine);
                                });
                    }
                    successCount++;
                } catch (Exception e) {
                    errors.add(new MedicineImportErrorDto(excelRowNum, e.getMessage() != null ? e.getMessage() : "Import failed"));
                }
            }

            result.setSuccessCount(successCount);
            result.setFailedCount(result.getTotalRows() - successCount);
            result.setErrors(errors);
            writeImportAudit(performedBy, result.getTotalRows(), successCount, result.getFailedCount(),
                    correlationId, file.getOriginalFilename(), "STOCK");

            log.info("pharm ({}) imported {} stock rows from Excel (correlationId={}, total={}, failed={})",
                    performedBy, successCount, correlationId, result.getTotalRows(), result.getFailedCount());
            return result;
        } catch (IOException e) {
            log.warn("Stock import failed (correlationId={}): {}", correlationId, e.getMessage());
            throw e;
        }
    }

    /** @deprecated Use importMasterFromExcel or importStockFromExcel */
    @Transactional(rollbackFor = Exception.class)
    public MedicineImportResultDto importFromExcel(MultipartFile file) throws IOException {
        return importMasterFromExcel(file);
    }

    private record StockImportRow(String medicineCode, String batchNo, LocalDate expiryDate, int quantity,
                                  BigDecimal costPrice, String rackCode) {}

    /**
     * Generate error report as Excel (failed rows with error messages).
     */
    public byte[] generateErrorReport(List<MedicineImportErrorDto> errors) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Import Errors");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Row");
            headerRow.createCell(1).setCellValue("Error");
            int r = 1;
            for (MedicineImportErrorDto err : errors) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(err.getRow());
                row.createCell(1).setCellValue(err.getError());
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are allowed");
        }
    }

    private void validateMasterHeaders(Row headerRow) {
        for (int i = 0; i < MASTER_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String val = getCellString(cell);
            if (val == null || !val.trim().equalsIgnoreCase(MASTER_HEADERS[i])) {
                throw new IllegalArgumentException("Invalid or missing column at " + (i + 1)
                        + ". Expected: " + MASTER_HEADERS[i] + ". Found: " + (val != null ? val : "empty"));
            }
        }
    }

    private void validateStockHeaders(Row headerRow) {
        for (int i = 0; i < STOCK_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String val = getCellString(cell);
            if (val == null || !val.trim().equalsIgnoreCase(STOCK_HEADERS[i])) {
                throw new IllegalArgumentException("Invalid or missing column at " + (i + 1)
                        + ". Expected: " + STOCK_HEADERS[i] + ". Found: " + (val != null ? val : "empty"));
            }
        }
    }

    private boolean isEmptyRow(Row row, int checkCols) {
        for (int i = 0; i < checkCols; i++) {
            Cell c = row.getCell(i);
            if (c != null) {
                String s = getCellString(c);
                if (s != null && !s.isBlank()) return false;
            }
        }
        return true;
    }

    private MedicineMaster parseAndValidateMasterRow(Row row, int excelRowNum,
                                                     Set<String> seenCodesInFile,
                                                     List<MedicineImportErrorDto> errors) {
        String medicineCode = trim(getCellString(row.getCell(0)));
        String medicineName = trim(getCellString(row.getCell(1)));
        String categoryStr = trim(getCellString(row.getCell(2)));
        String strength = trim(getCellString(row.getCell(3)));
        String formStr = trim(getCellString(row.getCell(4)));
        String minStockStr = trim(getCellString(row.getCell(5)));
        String lasaStr = trim(getCellString(row.getCell(6)));
        String storageStr = trim(getCellString(row.getCell(7)));
        String activeStr = trim(getCellString(row.getCell(8)));

        if (medicineCode == null || medicineCode.isEmpty()) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "MedicineCode is required"));
            return null;
        }
        if (seenCodesInFile.contains(medicineCode.toUpperCase())) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Duplicate MedicineCode in file"));
            return null;
        }
        if (medicineName == null || medicineName.isEmpty()) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "MedicineName is required"));
            return null;
        }

        MedicineCategory category = parseCategory(categoryStr);
        if (category == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Invalid Category. Use: Antibiotic, Analgesic, Cardiac, Diabetic, IV_FLUID, ICU_EMERGENCY, Other"));
            return null;
        }

        MedicineForm form = parseForm(formStr);
        if (form == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Invalid Form. Use: Tablet, Capsule, Injection, IV, Syrup, Ointment, Other"));
            return null;
        }

        Integer minStock = parseMinStock(minStockStr);
        if (minStock == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "MinStock must be a non-negative integer"));
            return null;
        }

        Boolean lasa = parseLasa(lasaStr);
        if (lasa == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "LASA must be Yes or No"));
            return null;
        }

        StorageType storageType = parseStorageType(storageStr);
        if (storageType == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Invalid StorageType. Use: RoomTemp or ColdChain"));
            return null;
        }

        Boolean active = parseActive(activeStr);
        if (active == null) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Active must be true or false"));
            return null;
        }

        MedicineMaster m = new MedicineMaster();
        m.setMedicineCode(medicineCode);
        m.setMedicineName(medicineName);
        m.setCategory(category);
        m.setStrength(strength);
        m.setForm(form);
        m.setMinStock(minStock);
        m.setQuantity(0);
        m.setLasaFlag(lasa);
        m.setStorageType(storageType);
        m.setActive(active);
        return m;
    }

    private StockImportRow parseAndValidateStockRow(Row row, int excelRowNum,
                                                     List<MedicineImportErrorDto> errors) {
        String medicineCode = trim(getCellString(row.getCell(0)));
        String batchNo = trim(getCellString(row.getCell(1)));
        String expiryStr = trim(getCellString(row.getCell(2)));
        String quantityStr = trim(getCellString(row.getCell(3)));
        String costPriceStr = trim(getCellString(row.getCell(4)));
        String rackCode = trim(getCellString(row.getCell(5)));

        if (medicineCode == null || medicineCode.isEmpty()) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "MedicineCode is required"));
            return null;
        }

        Integer quantity = parseMinStock(quantityStr);
        if (quantity == null || quantity < 1) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Quantity is mandatory and must be at least 1"));
            return null;
        }

        LocalDate expiryDate = parseExpiryDate(row.getCell(2), expiryStr, excelRowNum, errors);

        BigDecimal costPrice = null;
        if (costPriceStr != null && !costPriceStr.isEmpty()) {
            try {
                costPrice = new BigDecimal(costPriceStr.trim());
                if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(new MedicineImportErrorDto(excelRowNum, "CostPrice must be non-negative"));
                    return null;
                }
            } catch (NumberFormatException e) {
                errors.add(new MedicineImportErrorDto(excelRowNum, "Invalid CostPrice"));
                return null;
            }
        }

        return new StockImportRow(medicineCode, batchNo, expiryDate, quantity, costPrice, rackCode);
    }

    private LocalDate parseExpiryDate(Cell cell, String expiryStr, int excelRowNum, List<MedicineImportErrorDto> errors) {
        if (cell != null && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            try {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } catch (Exception ignored) {
            }
        }
        if (expiryStr == null || expiryStr.isEmpty()) return null;
        try {
            return LocalDate.parse(expiryStr.trim());
        } catch (Exception e) {
            errors.add(new MedicineImportErrorDto(excelRowNum, "Invalid ExpiryDate format. Use YYYY-MM-DD"));
            return null;
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private MedicineCategory parseCategory(String s) {
        if (s == null || s.isEmpty()) return null;
        String u = s.toUpperCase().replace(" ", "_");
        for (MedicineCategory c : MedicineCategory.values()) {
            if (c.name().equals(u) || c.name().replace("_", "").equals(u.replace("_", ""))) return c;
        }
        if ("ANTIBIOTIC".equalsIgnoreCase(u) || "Antibiotic".equalsIgnoreCase(s)) return MedicineCategory.ANTIBIOTIC;
        if ("ANALGESIC".equalsIgnoreCase(u) || "Analgesic".equalsIgnoreCase(s)) return MedicineCategory.ANALGESIC;
        if ("CARDIAC".equalsIgnoreCase(u) || "Cardiac".equalsIgnoreCase(s)) return MedicineCategory.CARDIAC;
        if ("DIABETIC".equalsIgnoreCase(u) || "Diabetic".equalsIgnoreCase(s)) return MedicineCategory.DIABETIC;
        if ("IVFLUID".equalsIgnoreCase(u.replace("_", "")) || "IV Fluid".equalsIgnoreCase(s)) return MedicineCategory.IV_FLUID;
        if ("ICUEMERGENCY".equalsIgnoreCase(u.replace("_", "")) || "ICU Emergency".equalsIgnoreCase(s)) return MedicineCategory.ICU_EMERGENCY;
        if ("OTHER".equalsIgnoreCase(u) || "Other".equalsIgnoreCase(s)) return MedicineCategory.OTHER;
        return null;
    }

    private MedicineForm parseForm(String s) {
        if (s == null || s.isEmpty()) return null;
        String u = s.toUpperCase().replace(" ", "_");
        for (MedicineForm f : MedicineForm.values()) {
            if (f.name().equals(u)) return f;
        }
        if ("TABLET".equalsIgnoreCase(u) || "Tablet".equalsIgnoreCase(s)) return MedicineForm.TABLET;
        if ("CAPSULE".equalsIgnoreCase(u) || "Capsule".equalsIgnoreCase(s)) return MedicineForm.CAPSULE;
        if ("INJECTION".equalsIgnoreCase(u) || "Injection".equalsIgnoreCase(s)) return MedicineForm.INJECTION;
        if ("IV".equalsIgnoreCase(u)) return MedicineForm.IV;
        if ("SYRUP".equalsIgnoreCase(u) || "Syrup".equalsIgnoreCase(s)) return MedicineForm.SYRUP;
        if ("OINTMENT".equalsIgnoreCase(u) || "Ointment".equalsIgnoreCase(s)) return MedicineForm.OINTMENT;
        if ("OTHER".equalsIgnoreCase(u) || "Other".equalsIgnoreCase(s)) return MedicineForm.OTHER;
        return null;
    }

    private Integer parseMinStock(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return v >= 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseLasa(String s) {
        if (s == null || s.isEmpty()) return null;
        if ("yes".equalsIgnoreCase(s.trim())) return true;
        if ("no".equalsIgnoreCase(s.trim())) return false;
        return null;
    }

    private StorageType parseStorageType(String s) {
        if (s == null || s.isEmpty()) return null;
        if ("RoomTemp".equalsIgnoreCase(s) || "ROOM_TEMP".equalsIgnoreCase(s) || "Room Temp".equalsIgnoreCase(s)) {
            return StorageType.ROOM_TEMP;
        }
        if ("ColdChain".equalsIgnoreCase(s) || "COLD_CHAIN".equalsIgnoreCase(s) || "Cold Chain".equalsIgnoreCase(s)) {
            return StorageType.COLD_CHAIN;
        }
        return null;
    }

    private Boolean parseActive(String s) {
        if (s == null || s.isEmpty()) return null;
        if ("true".equalsIgnoreCase(s.trim())) return true;
        if ("false".equalsIgnoreCase(s.trim())) return false;
        return null;
    }

    private void writeImportAudit(String performedBy, int totalRows, int successCount, int failedCount,
                                  String correlationId, String filename, String importType) {
        MedicineImportAuditLog audit = new MedicineImportAuditLog();
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(Instant.now());
        audit.setTotalRows(totalRows);
        audit.setSuccessCount(successCount);
        audit.setFailedCount(failedCount);
        audit.setCorrelationId(correlationId);
        audit.setFilename(filename);
        audit.setImportType(importType);
        importAuditRepository.save(audit);
    }

    private void writeEntryAudit(String performedBy, String correlationId, String excelFilename) {
        MedicineEntryAuditLog log = new MedicineEntryAuditLog();
        log.setEntryMode("IMPORT");
        log.setExcelFilename(excelFilename);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        log.setCorrelationId(correlationId);
        entryAuditRepository.save(log);
    }
}
