package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.config.AdmissionPriorityOverrideRoles;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.ipd.service.AdmissionPriorityApiService;
import com.hospital.hms.ipd.service.AdmissionPriorityAuditService;
import com.hospital.hms.ipd.service.AdmissionPriorityEvaluationService;
import com.hospital.hms.ipd.service.IPDAdmissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST API for Admission Priority.
 * <p>
 * Base path: /api (context) + /admission-priority (mapping). Full paths:
 * <ul>
 *   <li>GET  /api/admission-priority — list priority master (P1–P4)</li>
 *   <li>POST /api/admission-priority/evaluate — evaluate priority for a request</li>
 *   <li>POST /api/admission-priority/override — override admission priority (authority only)</li>
 *   <li>GET  /api/admission-priority/audit — read-only audit log (by admissionId or paginated)</li>
 * </ul>
 * <p>
 * Role-based access:
 * <ul>
 *   <li>GET, POST evaluate: ADMIN, IPD_MANAGER, DOCTOR, MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD</li>
 *   <li>POST override: MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER only</li>
 * </ul>
 * <p>
 * HTTP status codes: 200 OK, 201 Created (evaluate returns 200), 400 (validation), 403 (forbidden), 404 (not found).
 * DTO-based request/response; DB-agnostic.
 * <p>
 * Sample GET /api/admission-priority response:
 * <pre>
 * [
 *   { "id": 1, "priorityCode": "P1", "category": "EMERGENCY_CRITICAL", "description": "Emergency/Critical", "priorityOrder": 1, "active": true },
 *   { "id": 2, "priorityCode": "P2", "category": "SERIOUS", "description": "Serious", "priorityOrder": 2, "active": true }
 * ]
 * </pre>
 * <p>
 * Sample POST /api/admission-priority/evaluate request:
 * <pre>
 * {
 *   "admissionSource": "EMERGENCY",
 *   "wardType": "ICU",
 *   "referred": false,
 *   "seniorCitizen": true,
 *   "pregnantWoman": false,
 *   "child": false,
 *   "disabledPatient": false
 * }
 * </pre>
 * Sample evaluate response:
 * <pre>
 * { "priority": "P1", "assignmentReason": "Evaluated: EMERGENCY → P1" }
 * </pre>
 * <p>
 * Sample POST /api/admission-priority/override request:
 * <pre>
 * {
 *   "admissionId": 1,
 *   "newPriority": "P2",
 *   "reason": "Clinical escalation per consultant recommendation after review."
 * }
 * </pre>
 * Override response: full IPD admission DTO (200 OK).
 * <p>
 * Sample GET /api/admission-priority/audit?admissionId=1 response:
 * <pre>
 * [
 *   { "id": 1, "admissionId": 1, "priorityAssigned": "P1", "ruleApplied": "EMERGENCY", "specialConsiderationApplied": "SENIOR_CITIZEN",
 *     "isOverride": false, "overrideDetails": null, "approvedBy": null, "timestamp": "2025-01-30T10:00:00Z" },
 *   { "id": 2, "admissionId": 1, "priorityAssigned": "P2", "ruleApplied": null, "specialConsiderationApplied": null,
 *     "isOverride": true, "overrideDetails": "Clinical escalation.", "approvedBy": "medsuper", "timestamp": "2025-01-30T11:00:00Z" }
 * ]
 * </pre>
 */
@RestController
@RequestMapping("/admission-priority")
public class AdmissionPriorityController {

    private final AdmissionPriorityApiService priorityApiService;
    private final AdmissionPriorityEvaluationService evaluationService;
    private final IPDAdmissionService admissionService;
    private final AdmissionPriorityAuditService auditService;

    public AdmissionPriorityController(AdmissionPriorityApiService priorityApiService,
                                       AdmissionPriorityEvaluationService evaluationService,
                                       IPDAdmissionService admissionService,
                                       AdmissionPriorityAuditService auditService) {
        this.priorityApiService = priorityApiService;
        this.evaluationService = evaluationService;
        this.admissionService = admissionService;
        this.auditService = auditService;
    }

    /**
     * List admission priority master (P1–P4). Optional filter: activeOnly (default true).
     * Roles: ADMIN, IPD_MANAGER, DOCTOR, MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD.
     *
     * @param activeOnly if true (default), return only active priorities
     * @return 200 OK with list of priority items
     */
    @PreAuthorize(AdmissionPriorityOverrideRoles.CAN_READ_PRIORITY)
    @GetMapping
    public ResponseEntity<List<AdmissionPriorityItemDto>> list(
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        List<AdmissionPriorityItemDto> list = priorityApiService.list(activeOnly);
        return ResponseEntity.ok(list);
    }

    /**
     * Evaluate admission priority for the given request. Returns assigned priority (P1–P4) and reason.
     * Roles: ADMIN, IPD_MANAGER, DOCTOR, MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD.
     *
     * @param request evaluation input (all fields optional)
     * @return 200 OK with priority and assignmentReason
     */
    @PreAuthorize(AdmissionPriorityOverrideRoles.CAN_READ_PRIORITY)
    @PostMapping("/evaluate")
    public ResponseEntity<AdmissionPriorityEvaluateResponseDto> evaluate(
            @Valid @RequestBody AdmissionPriorityEvaluateRequestDto request) {
        AdmissionPriorityRequest evalRequest = toEvalRequest(request);
        var result = evaluationService.evaluateWithReason(evalRequest);
        AdmissionPriorityEvaluateResponseDto response = new AdmissionPriorityEvaluateResponseDto(
                result.getPriority(), result.getAssignmentReason());
        return ResponseEntity.ok(response);
    }

    /**
     * Override admission priority. Only MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER.
     * Override requires reason (10–500 chars); logged for audit.
     *
     * @param request admissionId, newPriority, reason
     * @param authentication current user (for authority check and audit)
     * @return 200 OK with updated IPD admission DTO
     */
    @PreAuthorize(AdmissionPriorityOverrideRoles.CAN_OVERRIDE_PRIORITY)
    @PostMapping("/override")
    public ResponseEntity<IPDAdmissionResponseDto> override(
            @Valid @RequestBody AdmissionPriorityOverrideApiRequestDto request,
            Authentication authentication) {
        AdmissionPriorityOverrideRequestDto body = new AdmissionPriorityOverrideRequestDto();
        body.setNewPriority(request.getNewPriority());
        body.setReason(request.getReason());
        IPDAdmissionResponseDto updated = admissionService.overridePriority(
                request.getAdmissionId(), body, authentication);
        return ResponseEntity.ok(updated);
    }

    /**
     * Read-only audit log for admission priority decisions. Track: priority assigned, rule applied,
     * special consideration, override details, approved by, timestamp.
     * Roles: same as read (ADMIN, IPD_MANAGER, DOCTOR, MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD).
     *
     * @param admissionId optional — when set, return list for this admission (newest first)
     * @param from        optional — start of timestamp range (ISO-8601)
     * @param to          optional — end of timestamp range (ISO-8601); used only when no admissionId
     * @param page        page index (default 0) when listing all
     * @param size        page size (default 20) when listing all
     * @return 200 OK — list when admissionId set, else paginated list
     */
    @PreAuthorize(AdmissionPriorityOverrideRoles.CAN_READ_PRIORITY)
    @GetMapping("/audit")
    public ResponseEntity<?> getAudit(
            @RequestParam(required = false) Long admissionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (admissionId != null) {
            List<AdmissionPriorityAuditResponseDto> list = auditService.getByAdmissionId(admissionId);
            return ResponseEntity.ok(list);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdmissionPriorityAuditResponseDto> result = auditService.getPage(null, from, to, pageable);
        return ResponseEntity.ok(result);
    }

    private static AdmissionPriorityRequest toEvalRequest(AdmissionPriorityEvaluateRequestDto dto) {
        AdmissionPriorityRequest r = new AdmissionPriorityRequest();
        r.setAdmissionSource(dto.getAdmissionSource());
        r.setWardType(dto.getWardType());
        r.setReferred(Boolean.TRUE.equals(dto.getReferred()));
        r.setSeniorCitizen(Boolean.TRUE.equals(dto.getSeniorCitizen()));
        r.setPregnantWoman(Boolean.TRUE.equals(dto.getPregnantWoman()));
        r.setChild(Boolean.TRUE.equals(dto.getChild()));
        r.setDisabledPatient(Boolean.TRUE.equals(dto.getDisabledPatient()));
        return r;
    }
}
