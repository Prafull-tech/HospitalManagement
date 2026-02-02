package com.hospital.hms.hospital.service;

import com.hospital.hms.common.exception.DuplicateBedAvailabilityException;
import com.hospital.hms.common.exception.InvalidBedCountsException;
import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.dto.BedAvailabilityAuditLogResponseDto;
import com.hospital.hms.hospital.dto.BedAvailabilityRequestDto;
import com.hospital.hms.hospital.dto.BedAvailabilityResponseDto;
import com.hospital.hms.hospital.entity.BedAvailability;
import com.hospital.hms.hospital.entity.BedAvailabilityAuditLog;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.BedAvailabilityAuditLogRepository;
import com.hospital.hms.hospital.repository.BedAvailabilityRepository;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Hospital-wise Bed Availability.
 * <ul>
 *   <li>Create, update, fetch by hospital, delete.</li>
 *   <li>Prevent duplicate Hospital + WardType.</li>
 *   <li>Vacant beds auto-calculated (not stored).</li>
 *   <li>Validate bed counts (non-negative, sum ≤ totalBeds).</li>
 *   <li>Role logic (logical, not security): ADMIN full, IPD_MANAGER update only, DOCTOR read only.</li>
 * </ul>
 */
@Service
public class BedAvailabilityService {

    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";

    private final BedAvailabilityRepository bedAvailabilityRepository;
    private final BedAvailabilityAuditLogRepository auditLogRepository;
    private final HospitalService hospitalService;

    public BedAvailabilityService(BedAvailabilityRepository bedAvailabilityRepository,
                                  BedAvailabilityAuditLogRepository auditLogRepository,
                                  HospitalService hospitalService) {
        this.bedAvailabilityRepository = bedAvailabilityRepository;
        this.auditLogRepository = auditLogRepository;
        this.hospitalService = hospitalService;
    }

    // --- Read (all roles) ---

    /**
     * Fetch all bed availability records for a hospital. Read-only; allowed for ADMIN, IPD_MANAGER, DOCTOR.
     */
    @Transactional(readOnly = true)
    public List<BedAvailabilityResponseDto> listByHospitalId(Long hospitalId) {
        Hospital hospital = hospitalService.getEntityById(hospitalId);
        return bedAvailabilityRepository.findByHospitalIdOrderByWardTypeAsc(hospital.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single bed availability by id within a hospital. Read-only; allowed for all roles.
     */
    @Transactional(readOnly = true)
    public BedAvailabilityResponseDto getById(Long hospitalId, Long id) {
        hospitalService.getEntityById(hospitalId);
        BedAvailability ba = findEntityOrThrow(id);
        ensureSameHospital(ba, hospitalId, id);
        return toDto(ba);
    }

    /**
     * Read-only audit trail for a bed availability record. Who changed, when, which role/action.
     */
    @Transactional(readOnly = true)
    public List<BedAvailabilityAuditLogResponseDto> getAuditTrail(Long hospitalId, Long id) {
        hospitalService.getEntityById(hospitalId);
        BedAvailability ba = findEntityOrThrow(id);
        ensureSameHospital(ba, hospitalId, id);
        return auditLogRepository.findByBedAvailabilityIdOrderByChangedAtDesc(ba.getId())
                .stream()
                .map(this::toAuditDto)
                .collect(Collectors.toList());
    }

    // --- Create (ADMIN only) ---

    /**
     * Create bed availability. Duplicate Hospital + WardType is rejected. Vacant is auto-calculated.
     *
     * @throws OperationNotAllowedException if callerRole is not ADMIN
     * @throws DuplicateBedAvailabilityException if a record for this hospital + wardType already exists
     * @throws InvalidBedCountsException if counts are invalid
     */
    @Transactional
    public BedAvailabilityResponseDto create(Long hospitalId, BedAvailabilityRequestDto request,
                                             BedAvailabilityCallerRole callerRole, String updatedBy) {
        ensureCanCreate(callerRole);
        Hospital hospital = hospitalService.getEntityById(hospitalId);
        WardType wardType = parseWardType(request.getWardType());
        validateBedCounts(request);

        if (bedAvailabilityRepository.existsByHospitalAndWardType(hospital, wardType)) {
            throw new DuplicateBedAvailabilityException(
                    "Bed availability for ward type " + wardType + " already exists for this hospital.");
        }

        BedAvailability ba = new BedAvailability();
        ba.setHospital(hospital);
        ba.setWardType(wardType);
        ba.setTotalBeds(request.getTotalBeds());
        ba.setOccupiedBeds(request.getOccupiedBeds());
        ba.setReservedBeds(request.getReservedBeds());
        ba.setUnderCleaningBeds(request.getUnderCleaningBeds());
        ba.setUpdatedBy(updatedBy);
        ba = bedAvailabilityRepository.save(ba);
        appendAuditLog(ba, updatedBy, callerRole.name(), ACTION_CREATE);
        return toDto(ba);
    }

    // --- Update (ADMIN, IPD_MANAGER) ---

    /**
     * Update bed availability. Duplicate Hospital + WardType (after change) is rejected. Vacant is auto-calculated.
     *
     * @throws OperationNotAllowedException if callerRole is DOCTOR
     * @throws DuplicateBedAvailabilityException if another record for this hospital + wardType exists (when changing ward type)
     * @throws InvalidBedCountsException if counts are invalid
     */
    @Transactional
    public BedAvailabilityResponseDto update(Long hospitalId, Long id, BedAvailabilityRequestDto request,
                                             BedAvailabilityCallerRole callerRole, String updatedBy) {
        ensureCanUpdate(callerRole);
        hospitalService.getEntityById(hospitalId);
        WardType wardType = parseWardType(request.getWardType());
        validateBedCounts(request);

        BedAvailability ba = findEntityOrThrow(id);
        ensureSameHospital(ba, hospitalId, id);

        if (bedAvailabilityRepository.existsByHospitalIdAndWardTypeAndIdNot(hospitalId, wardType, id)) {
            throw new DuplicateBedAvailabilityException(
                    "Another record for ward type " + wardType + " already exists for this hospital.");
        }

        ba.setWardType(wardType);
        ba.setTotalBeds(request.getTotalBeds());
        ba.setOccupiedBeds(request.getOccupiedBeds());
        ba.setReservedBeds(request.getReservedBeds());
        ba.setUnderCleaningBeds(request.getUnderCleaningBeds());
        ba.setUpdatedBy(updatedBy);
        ba = bedAvailabilityRepository.save(ba);
        appendAuditLog(ba, updatedBy, callerRole.name(), ACTION_UPDATE);
        return toDto(ba);
    }

    // --- Delete (ADMIN only) ---

    /**
     * Delete bed availability.
     *
     * @throws OperationNotAllowedException if callerRole is not ADMIN
     */
    @Transactional
    public void delete(Long hospitalId, Long id, BedAvailabilityCallerRole callerRole) {
        ensureCanDelete(callerRole);
        hospitalService.getEntityById(hospitalId);
        BedAvailability ba = findEntityOrThrow(id);
        ensureSameHospital(ba, hospitalId, id);
        bedAvailabilityRepository.delete(ba);
    }

    // --- Role checks (logical, not security) ---

    private static void ensureCanCreate(BedAvailabilityCallerRole callerRole) {
        if (callerRole != BedAvailabilityCallerRole.ADMIN) {
            throw new OperationNotAllowedException(
                    "Create is not allowed for role " + callerRole + ". Only ADMIN can create bed availability.");
        }
    }

    private static void ensureCanUpdate(BedAvailabilityCallerRole callerRole) {
        if (callerRole == BedAvailabilityCallerRole.DOCTOR) {
            throw new OperationNotAllowedException(
                    "Update is not allowed for role DOCTOR. ADMIN and IPD_MANAGER can update.");
        }
    }

    private static void ensureCanDelete(BedAvailabilityCallerRole callerRole) {
        if (callerRole != BedAvailabilityCallerRole.ADMIN) {
            throw new OperationNotAllowedException(
                    "Delete is not allowed for role " + callerRole + ". Only ADMIN can delete bed availability.");
        }
    }

    // --- Validation ---

    private static void validateBedCounts(BedAvailabilityRequestDto request) {
        int total = request.getTotalBeds() != null ? request.getTotalBeds() : 0;
        int occupied = request.getOccupiedBeds() != null ? request.getOccupiedBeds() : 0;
        int reserved = request.getReservedBeds() != null ? request.getReservedBeds() : 0;
        int cleaning = request.getUnderCleaningBeds() != null ? request.getUnderCleaningBeds() : 0;

        if (occupied < 0 || reserved < 0 || cleaning < 0 || total < 0) {
            throw new InvalidBedCountsException("Bed counts cannot be negative.");
        }
        if (occupied + reserved + cleaning > total) {
            throw new InvalidBedCountsException(
                    "Occupied + reserved + under cleaning must not exceed total beds.");
        }
    }

    private static WardType parseWardType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ward type is required.");
        }
        try {
            return WardType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid ward type: " + value + ". Valid values: " + Arrays.toString(WardType.values()));
        }
    }

    // --- Helpers ---

    private BedAvailability findEntityOrThrow(Long id) {
        return bedAvailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed availability not found: " + id));
    }

    private static void ensureSameHospital(BedAvailability ba, Long hospitalId, Long id) {
        if (!ba.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Bed availability not found: " + id);
        }
    }

    private void appendAuditLog(BedAvailability ba, String changedBy, String performedByRole, String action) {
        BedAvailabilityAuditLog log = new BedAvailabilityAuditLog();
        log.setBedAvailability(ba);
        log.setChangedAt(Instant.now());
        log.setChangedBy(changedBy != null ? changedBy : "system");
        log.setPerformedByRole(performedByRole);
        log.setAction(action);
        auditLogRepository.save(log);
    }

    private BedAvailabilityResponseDto toDto(BedAvailability ba) {
        BedAvailabilityResponseDto dto = new BedAvailabilityResponseDto();
        dto.setId(ba.getId());
        dto.setHospitalId(ba.getHospital().getId());
        dto.setWardType(ba.getWardType().name());
        dto.setTotalBeds(ba.getTotalBeds());
        dto.setOccupied(ba.getOccupiedBeds());
        dto.setVacant(ba.getVacant());
        dto.setReserved(ba.getReservedBeds());
        dto.setUnderCleaning(ba.getUnderCleaningBeds());
        dto.setCreatedAt(ba.getCreatedAt());
        dto.setUpdatedAt(ba.getUpdatedAt());
        dto.setUpdatedBy(ba.getUpdatedBy());
        return dto;
    }

    private BedAvailabilityAuditLogResponseDto toAuditDto(BedAvailabilityAuditLog log) {
        BedAvailabilityAuditLogResponseDto dto = new BedAvailabilityAuditLogResponseDto();
        dto.setId(log.getId());
        dto.setBedAvailabilityId(log.getBedAvailability().getId());
        dto.setChangedAt(log.getChangedAt());
        dto.setChangedBy(log.getChangedBy());
        dto.setPerformedByRole(log.getPerformedByRole());
        dto.setAction(log.getAction());
        return dto;
    }
}
