package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.dto.AdmissionStatusMasterDto;
import com.hospital.hms.ipd.dto.IPDAdmissionResponseDto;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.AdmissionStatusAuditLog;
import com.hospital.hms.ipd.service.AdmissionStatusMasterService;
import com.hospital.hms.ipd.service.IPDAdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IPD Admission Status Master API.
 * <ul>
 *   <li>GET /api/ipd/status-master — list all statuses (ACTIVE, DISCHARGED, SHIFTED/TRANSFERRED, REFERRED, LAMA, EXPIRED, etc.)</li>
 *   <li>GET /api/ipd/status-master/transitions?from=ACTIVE — allowed transitions from a status</li>
 *   <li>PATCH /api/ipd/status-master/admissions/{id}/status — change status (validated + audited)</li>
 *   <li>GET /api/ipd/status-master/admissions/{id}/audit — status change audit log</li>
 * </ul>
 */
@RestController
@RequestMapping("/ipd/status-master")
public class AdmissionStatusMasterController {

    private final AdmissionStatusMasterService statusMasterService;
    private final IPDAdmissionService admissionService;

    public AdmissionStatusMasterController(AdmissionStatusMasterService statusMasterService,
                                           IPDAdmissionService admissionService) {
        this.statusMasterService = statusMasterService;
        this.admissionService = admissionService;
    }

    /**
     * List statuses. Use ?masterOnly=true to return only the 6 primary statuses:
     * ACTIVE, DISCHARGED, SHIFTED, REFERRED, LAMA, EXPIRED.
     */
    @GetMapping
    public ResponseEntity<Set<AdmissionStatusMasterDto.StatusItem>> listStatuses(
            @RequestParam(required = false, defaultValue = "false") boolean masterOnly) {
        Set<AdmissionStatus> statuses = masterOnly
                ? statusMasterService.getMasterStatusesOnly()
                : statusMasterService.getAllStatuses();
        Set<AdmissionStatusMasterDto.StatusItem> items = statuses.stream()
                .map(AdmissionStatusMasterDto.StatusItem::of)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/transitions")
    public ResponseEntity<AdmissionStatusMasterDto.AllowedTransitionsResponse> getAllowedTransitions(
            @RequestParam(required = false) AdmissionStatus from) {
        Set<AdmissionStatus> allowed = statusMasterService.getAllowedTransitions(from);
        AdmissionStatusMasterDto.AllowedTransitionsResponse response =
                AdmissionStatusMasterDto.AllowedTransitionsResponse.of(from, allowed);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admissions/{admissionId}/status")
    public ResponseEntity<IPDAdmissionResponseDto> changeStatus(
            @PathVariable Long admissionId,
            @Valid @RequestBody AdmissionStatusMasterDto.ChangeStatusRequest request,
            Authentication authentication) {
        statusMasterService.changeStatus(
                admissionId,
                request.getToStatus(),
                request.getReason(),
                authentication);
        IPDAdmissionResponseDto admission = admissionService.getById(admissionId);
        return ResponseEntity.ok(admission);
    }

    @GetMapping("/admissions/{admissionId}/audit")
    public ResponseEntity<List<AdmissionStatusMasterDto.AuditLogItem>> getAuditLog(@PathVariable Long admissionId) {
        List<AdmissionStatusAuditLog> logs = statusMasterService.getAuditLogByAdmissionId(admissionId);
        List<AdmissionStatusMasterDto.AuditLogItem> items = logs.stream()
                .map(AdmissionStatusMasterController::toAuditItem)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    private static AdmissionStatusMasterDto.AuditLogItem toAuditItem(AdmissionStatusAuditLog log) {
        AdmissionStatusMasterDto.AuditLogItem item = new AdmissionStatusMasterDto.AuditLogItem();
        item.setAdmissionId(log.getAdmissionId());
        item.setFromStatus(log.getFromStatus() != null ? log.getFromStatus().name() : null);
        item.setToStatus(log.getToStatus().name());
        item.setFromStatusDisplay(log.getFromStatus() != null ? AdmissionStatusMasterDto.StatusItem.displayNameFor(log.getFromStatus()) : null);
        item.setToStatusDisplay(AdmissionStatusMasterDto.StatusItem.displayNameFor(log.getToStatus()));
        item.setChangedAt(log.getChangedAt());
        item.setChangedBy(log.getChangedBy());
        item.setReason(log.getReason());
        return item;
    }
}
