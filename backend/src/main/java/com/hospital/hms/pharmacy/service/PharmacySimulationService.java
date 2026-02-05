package com.hospital.hms.pharmacy.service;

import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.pharmacy.dto.ExpiryAlertDto;
import com.hospital.hms.pharmacy.dto.FefoStockRowDto;
import com.hospital.hms.pharmacy.dto.IpdIssueQueueItemDto;
import com.hospital.hms.pharmacy.dto.IpdIssueQueueLineDto;
import com.hospital.hms.pharmacy.dto.PharmacySummaryDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Simulation-only pharmacy service to drive the Pharmacy Dashboard UI
 * until full inventory & pharmacy entities are implemented.
 *
 * IMPORTANT: This service keeps data in-memory only and does not persist to DB.
 */
@Service
public class PharmacySimulationService {

    private final IPDAdmissionRepository admissionRepository;
    private final ConcurrentHashMap<Long, IpdIssueQueueItemDto> queue = new ConcurrentHashMap<>();
    private final AtomicLong alertSeq = new AtomicLong(1);

    public PharmacySimulationService(IPDAdmissionRepository admissionRepository) {
        this.admissionRepository = admissionRepository;
        seedInitialData();
    }

    private void seedInitialData() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<IPDAdmission> admissions = admissionRepository.findAll()
                .stream().limit(3).collect(Collectors.toList());
        long id = 1;
        for (IPDAdmission adm : admissions) {
            IpdIssueQueueItemDto item = new IpdIssueQueueItemDto();
            item.setIndentId(id++);
            item.setIpdAdmissionId(adm.getId());
            item.setIpdAdmissionNumber(adm.getAdmissionNumber());
            item.setPatientName(adm.getPatient().getFullName());
            // For simulation, ward/bed are not available directly on entity; mark as placeholders.
            item.setWardName("Ward");
            item.setBedNumber("Bed");
            item.setPriority("ICU");
            item.setOrderedAtDisplay(LocalDateTime.now().minusMinutes(20).format(dtf));
            item.setWaitingMinutes(20);
            item.setStatus("PENDING");

            List<IpdIssueQueueLineDto> lines = new ArrayList<>();
            IpdIssueQueueLineDto l1 = new IpdIssueQueueLineDto();
            l1.setMedicineCode("CEFTRIAXONE_1G_IV");
            l1.setMedicineName("Ceftriaxone 1g IV");
            l1.setRequestedQty(2);
            l1.setAvailableQty(20);
            l1.setNextBatchNumber("CFTX-001");
            l1.setNextBatchExpiryDisplay(LocalDate.now().plusDays(25).toString());
            l1.setExpiryRiskClass("text-warning");
            l1.setLasa(false);
            lines.add(l1);

            IpdIssueQueueLineDto l2 = new IpdIssueQueueLineDto();
            l2.setMedicineCode("PARACETAMOL_500_T");
            l2.setMedicineName("Paracetamol 500mg");
            l2.setRequestedQty(10);
            l2.setAvailableQty(200);
            l2.setNextBatchNumber("PCM-045");
            l2.setNextBatchExpiryDisplay(LocalDate.now().plusMonths(6).toString());
            l2.setExpiryRiskClass("text-success");
            l2.setLasa(false);
            lines.add(l2);

            item.setLines(lines);
            item.setMedicineCount(lines.size());
            queue.put(item.getIndentId(), item);
        }
    }

    public List<IpdIssueQueueItemDto> getIssueQueue(String q) {
        List<IpdIssueQueueItemDto> values = new ArrayList<>(queue.values());
        if (q != null && !q.isBlank()) {
            String lower = q.toLowerCase();
            values = values.stream()
                    .filter(i -> (i.getIpdAdmissionNumber() != null && i.getIpdAdmissionNumber().toLowerCase().contains(lower))
                            || (i.getPatientName() != null && i.getPatientName().toLowerCase().contains(lower))
                            || (i.getWardName() != null && i.getWardName().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }
        values.sort(Comparator.comparing(IpdIssueQueueItemDto::getPriority).thenComparing(IpdIssueQueueItemDto::getWaitingMinutes).reversed());
        return values;
    }

    public void issueIndent(Long indentId, boolean partial) {
        IpdIssueQueueItemDto item = queue.remove(indentId);
        if (item == null) {
            return;
        }
        // In simulation we just drop it from queue; in real implementation, stock will be deducted here.
    }

    public List<FefoStockRowDto> getFefoStock(String q, String risk) {
        List<FefoStockRowDto> list = new ArrayList<>();
        FefoStockRowDto r1 = new FefoStockRowDto();
        r1.setMedicineCode("CEFTRIAXONE_1G_IV");
        r1.setMedicineName("Ceftriaxone 1g IV");
        r1.setBatchNumber("CFTX-001");
        r1.setExpiryDate(LocalDate.now().plusDays(25).toString());
        r1.setQuantityAvailable(20);
        r1.setFefoRank(1);
        r1.setRiskLevel("NEAR_EXPIRY");
        r1.setRiskColorClass("text-warning");
        r1.setLasa(false);
        r1.setStorageLocation("IPD Pharmacy");
        list.add(r1);

        FefoStockRowDto r2 = new FefoStockRowDto();
        r2.setMedicineCode("ADRENALINE_1MG");
        r2.setMedicineName("Adrenaline 1mg/mL");
        r2.setBatchNumber("ADR-010");
        r2.setExpiryDate(LocalDate.now().minusDays(5).toString());
        r2.setQuantityAvailable(5);
        r2.setFefoRank(1);
        r2.setRiskLevel("EXPIRED");
        r2.setRiskColorClass("text-muted");
        r2.setLasa(true);
        r2.setStorageLocation("ICU Store");
        list.add(r2);

        if (q != null && !q.isBlank()) {
            String lower = q.toLowerCase();
            list = list.stream()
                    .filter(r -> r.getMedicineName().toLowerCase().contains(lower)
                            || r.getBatchNumber().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        if (risk != null && !risk.isBlank()) {
            list = list.stream()
                    .filter(r -> risk.equalsIgnoreCase(r.getRiskLevel()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public List<ExpiryAlertDto> getAlerts() {
        List<ExpiryAlertDto> list = new ArrayList<>();
        ExpiryAlertDto a1 = new ExpiryAlertDto();
        a1.setId(alertSeq.getAndIncrement());
        a1.setMedicineCode("ADRENALINE_1MG");
        a1.setMedicineName("Adrenaline 1mg/mL");
        a1.setBatchNumber("ADR-010");
        a1.setExpiryDate(LocalDate.now().minusDays(5).toString());
        a1.setQuantityRemaining(5);
        a1.setRiskLevel("EXPIRED");
        a1.setSeverity("CRITICAL");
        a1.setStorageLocation("ICU Store");
        a1.setAcknowledged(false);
        a1.setCreatedAt(LocalDateTime.now().minusHours(4).toString());
        list.add(a1);

        ExpiryAlertDto a2 = new ExpiryAlertDto();
        a2.setId(alertSeq.getAndIncrement());
        a2.setMedicineCode("CEFTRIAXONE_1G_IV");
        a2.setMedicineName("Ceftriaxone 1g IV");
        a2.setBatchNumber("CFTX-001");
        a2.setExpiryDate(LocalDate.now().plusDays(25).toString());
        a2.setQuantityRemaining(20);
        a2.setRiskLevel("NEAR_EXPIRY");
        a2.setSeverity("WARNING");
        a2.setStorageLocation("IPD Pharmacy");
        a2.setAcknowledged(false);
        a2.setCreatedAt(LocalDateTime.now().minusHours(2).toString());
        list.add(a2);

        return list;
    }

    public ExpiryAlertDto acknowledgeAlert(Long id) {
        // In simulation we simply return a dummy acknowledged alert.
        ExpiryAlertDto dto = new ExpiryAlertDto();
        dto.setId(id);
        dto.setAcknowledged(true);
        dto.setCreatedAt(LocalDateTime.now().toString());
        dto.setMedicineName("Acknowledged alert");
        return dto;
    }

    public PharmacySummaryDto getTodaySummary() {
        PharmacySummaryDto dto = new PharmacySummaryDto();
        dto.setDate(LocalDate.now().toString());
        dto.setTotalIndentsReceived(10);
        dto.setTotalIndentsIssued(8);
        dto.setPendingIndents(2);
        dto.setMedicinesIssuedCount(40);
        dto.setStockAdjustmentsCount(1);
        dto.setOverridesCount(0);
        dto.setHighRiskAlerts(2);
        return dto;
    }
}

