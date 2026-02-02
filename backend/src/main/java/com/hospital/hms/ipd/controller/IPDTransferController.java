package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.config.TransferRoles;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.ipd.service.TransferWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for patient transfer & upgrade.
 * <p>
 * Base path: /api (context) + /ipd/transfers (mapping). Full paths:
 * <ul>
 *   <li>POST /api/ipd/transfers/recommend</li>
 *   <li>POST /api/ipd/transfers/consent</li>
 *   <li>POST /api/ipd/transfers/confirm-bed</li>
 *   <li>POST /api/ipd/transfers/execute</li>
 *   <li>GET  /api/ipd/transfers/{ipdAdmissionId}</li>
 * </ul>
 * <p>
 * Access: DOCTOR → recommend; IPD_MANAGER → approve (consent, confirm-bed); NURSE → execute; ADMIN → full.
 * <p>
 * Sample POST /api/ipd/transfers/recommend request:
 * <pre>
 * {
 *   "ipdAdmissionId": 1,
 *   "recommendedByDoctorId": 1,
 *   "fromWardType": "GENERAL",
 *   "toWardType": "ICU",
 *   "indicationId": null,
 *   "recommendationNotes": "Patient requires ICU monitoring.",
 *   "emergencyFlag": false
 * }
 * </pre>
 * Sample recommend response:
 * <pre>
 * {
 *   "id": 1,
 *   "ipdAdmissionId": 1,
 *   "recommendedByDoctorId": 1,
 *   "fromWardType": "GENERAL",
 *   "toWardType": "ICU",
 *   "indicationId": null,
 *   "recommendationNotes": "Patient requires ICU monitoring.",
 *   "emergencyFlag": false,
 *   "recommendationTime": "2025-01-30T12:00:00Z"
 * }
 * </pre>
 * <p>
 * Sample POST /api/ipd/transfers/consent request:
 * <pre>
 * {
 *   "transferRecommendationId": 1,
 *   "consentGiven": true,
 *   "consentByName": "John Doe",
 *   "relationToPatient": "Son",
 *   "consentMode": "WRITTEN"
 * }
 * </pre>
 * <p>
 * Sample POST /api/ipd/transfers/confirm-bed request:
 * <pre>
 * {
 *   "transferRecommendationId": 1,
 *   "newBedId": 5
 * }
 * </pre>
 * <p>
 * Sample POST /api/ipd/transfers/execute request:
 * <pre>
 * {
 *   "transferRecommendationId": 1,
 *   "nurseId": 1,
 *   "attendantId": null,
 *   "equipmentUsed": "OXYGEN",
 *   "transferStatus": "COMPLETED"
 * }
 * </pre>
 * <p>
 * Sample GET /api/ipd/transfers/1 response:
 * <pre>
 * {
 *   "ipdAdmissionId": 1,
 *   "recommendations": [ { "id": 1, "ipdAdmissionId": 1, ... } ],
 *   "consents": [ { "id": 1, "transferRecommendationId": 1, "consentGiven": true, ... } ],
 *   "bedReservations": [ { "id": 1, "newBedId": 5, "reservationStatus": "RESERVED", ... } ],
 *   "executions": [ { "id": 1, "transferStatus": "COMPLETED", ... } ]
 * }
 * </pre>
 */
@RestController
@RequestMapping("/ipd/transfers")
public class IPDTransferController {

    private final TransferWorkflowService transferWorkflowService;

    public IPDTransferController(TransferWorkflowService transferWorkflowService) {
        this.transferWorkflowService = transferWorkflowService;
    }

    @PreAuthorize(TransferRoles.CAN_RECOMMEND)
    @PostMapping("/recommend")
    public ResponseEntity<TransferRecommendResponseDto> recommend(@Valid @RequestBody TransferRecommendRequestDto request) {
        TransferRecommendResponseDto created = transferWorkflowService.recommend(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize(TransferRoles.CAN_APPROVE)
    @PostMapping("/consent")
    public ResponseEntity<TransferConsentResponseDto> consent(@Valid @RequestBody TransferConsentRequestDto request) {
        TransferConsentResponseDto created = transferWorkflowService.recordConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize(TransferRoles.CAN_APPROVE)
    @PostMapping("/confirm-bed")
    public ResponseEntity<ConfirmBedResponseDto> confirmBed(@Valid @RequestBody ConfirmBedRequestDto request) {
        ConfirmBedResponseDto created = transferWorkflowService.confirmBed(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize(TransferRoles.CAN_EXECUTE)
    @PostMapping("/execute")
    public ResponseEntity<ExecuteTransferResponseDto> execute(@Valid @RequestBody ExecuteTransferRequestDto request) {
        ExecuteTransferResponseDto result = transferWorkflowService.execute(request);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize(TransferRoles.CAN_READ)
    @GetMapping("/{ipdAdmissionId}")
    public ResponseEntity<TransferSummaryResponseDto> getByAdmissionId(@PathVariable Long ipdAdmissionId) {
        TransferSummaryResponseDto summary = transferWorkflowService.getTransfersByAdmissionId(ipdAdmissionId);
        return ResponseEntity.ok(summary);
    }
}
