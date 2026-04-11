package com.hospital.hms.enquiry.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import com.hospital.hms.enquiry.dto.EnquiryAssignRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryAuditLogDto;
import com.hospital.hms.enquiry.dto.EnquiryDashboardDto;
import com.hospital.hms.enquiry.dto.EnquiryNoteRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryRequestDto;
import com.hospital.hms.enquiry.dto.EnquiryResponseDto;
import com.hospital.hms.enquiry.dto.EnquiryStatusUpdateRequestDto;
import com.hospital.hms.enquiry.entity.Enquiry;
import com.hospital.hms.enquiry.entity.EnquiryAuditEventType;
import com.hospital.hms.enquiry.entity.EnquiryAuditLog;
import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import com.hospital.hms.enquiry.entity.EnquiryStatus;
import com.hospital.hms.enquiry.repository.EnquiryAuditLogRepository;
import com.hospital.hms.enquiry.repository.EnquiryRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class EnquiryService {

    private static final DateTimeFormatter ENQUIRY_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final EnquiryRepository enquiryRepository;
    private final EnquiryAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final MedicalDepartmentRepository departmentRepository;

    public EnquiryService(EnquiryRepository enquiryRepository,
                          EnquiryAuditLogRepository auditLogRepository,
                          PatientRepository patientRepository,
                          MedicalDepartmentRepository departmentRepository) {
        this.enquiryRepository = enquiryRepository;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public EnquiryResponseDto create(EnquiryRequestDto request) {
        Enquiry enquiry = new Enquiry();
        enquiry.setEnquiryNo(generateEnquiryNo());
        applyCreateRequest(enquiry, request);
        enquiry.setStatus(EnquiryStatus.OPEN);
        enquiry = enquiryRepository.save(enquiry);
        audit(enquiry.getId(), EnquiryAuditEventType.CREATED, request.getDescription());
        return toDto(enquiry, true);
    }

    @Transactional(readOnly = true)
    public EnquiryResponseDto getById(Long id) {
        Enquiry enquiry = enquiryRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
        return toDto(enquiry, true);
    }

    @Transactional(readOnly = true)
    public Page<EnquiryResponseDto> search(EnquiryStatus status,
                                           EnquiryCategory category,
                                           EnquiryPriority priority,
                                           Long departmentId,
                                           String assignedToUser,
                                           LocalDate createdFrom,
                                           LocalDate createdTo,
                                           String patientUhid,
                                           String query,
                                           int page,
                                           int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Instant from = createdFrom != null
                ? createdFrom.atStartOfDay(ZoneId.systemDefault()).toInstant()
                : null;
        Instant to = createdTo != null
                ? createdTo.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                : null;
        return enquiryRepository.search(
                status,
                category,
                priority,
                departmentId,
                blankToNull(assignedToUser),
                from,
                to,
                blankToNull(patientUhid),
                normalizeQuery(query),
                pageable
        ).map(enquiry -> toDto(enquiry, false));
    }

    @Transactional(readOnly = true)
    public EnquiryDashboardDto getDashboard() {
        EnquiryDashboardDto dto = new EnquiryDashboardDto();
        dto.setOpenCount(enquiryRepository.countByStatus(EnquiryStatus.OPEN));
        dto.setInProgressCount(enquiryRepository.countByStatus(EnquiryStatus.IN_PROGRESS));
        dto.setResolvedCount(enquiryRepository.countByStatus(EnquiryStatus.RESOLVED));
        dto.setClosedCount(enquiryRepository.countByStatus(EnquiryStatus.CLOSED));
        dto.setEscalatedCount(enquiryRepository.countByStatus(EnquiryStatus.ESCALATED));
        dto.setRecentEnquiries(enquiryRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(enquiry -> toDto(enquiry, false))
                .collect(Collectors.toList()));
        dto.setByCategory(enquiryRepository.countByCategory().stream().collect(Collectors.toMap(
                row -> String.valueOf(row[0]),
                row -> (Long) row[1]
        )));
        return dto;
    }

    @Transactional
    public EnquiryResponseDto assign(Long id, EnquiryAssignRequestDto request) {
        Enquiry enquiry = enquiryRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
        if (request.getDepartmentId() != null) {
            MedicalDepartment department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            enquiry.setDepartment(department);
        }
        enquiry.setAssignedToUser(blankToNull(request.getAssignedToUser()));
        if (enquiry.getStatus() == EnquiryStatus.OPEN) {
            enquiry.setStatus(EnquiryStatus.IN_PROGRESS);
        }
        enquiry = enquiryRepository.save(enquiry);
        audit(enquiry.getId(), EnquiryAuditEventType.ASSIGNED, request.getNote());
        return toDto(enquiry, true);
    }

    @Transactional
    public EnquiryResponseDto updateStatus(Long id, EnquiryStatusUpdateRequestDto request) {
        Enquiry enquiry = enquiryRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
        EnquiryStatus newStatus = request.getStatus();
        enquiry.setStatus(newStatus);

        String resolution = blankToNull(request.getResolution());
        if (newStatus == EnquiryStatus.RESOLVED) {
            if (resolution == null) {
                throw new IllegalArgumentException("Resolution is required when resolving an enquiry");
            }
            enquiry.setResolution(resolution);
            enquiry.setResolvedAt(Instant.now());
        } else if (newStatus == EnquiryStatus.CLOSED || newStatus == EnquiryStatus.ESCALATED) {
            enquiry.setResolvedAt(null);
            if (resolution != null) {
                enquiry.setResolution(resolution);
            }
        } else {
            enquiry.setResolvedAt(null);
            if (resolution != null) {
                enquiry.setResolution(resolution);
            }
        }

        enquiry = enquiryRepository.save(enquiry);
        audit(enquiry.getId(), eventForStatus(newStatus), blankToNull(request.getNote()));
        return toDto(enquiry, true);
    }

    @Transactional
    public EnquiryResponseDto addNote(Long id, EnquiryNoteRequestDto request) {
        Enquiry enquiry = enquiryRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found: " + id));
        audit(enquiry.getId(), EnquiryAuditEventType.NOTE_ADDED, request.getNote());
        return toDto(enquiry, true);
    }

    private void applyCreateRequest(Enquiry enquiry, EnquiryRequestDto request) {
        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        }
        enquiry.setPatient(patient);
        enquiry.setDepartment(resolveDepartment(request.getDepartmentId()));
        enquiry.setCategory(request.getCategory());
        enquiry.setPriority(request.getPriority() != null ? request.getPriority() : EnquiryPriority.MEDIUM);
        enquiry.setSubject(request.getSubject().trim());
        enquiry.setDescription(request.getDescription().trim());
        enquiry.setEnquirerName(blankToNull(request.getEnquirerName()));
        enquiry.setPhone(blankToNull(request.getPhone()));
        enquiry.setEmail(blankToNull(request.getEmail()));
    }

    private MedicalDepartment resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
    }

    private EnquiryResponseDto toDto(Enquiry enquiry, boolean includeAuditLogs) {
        EnquiryResponseDto dto = new EnquiryResponseDto();
        dto.setId(enquiry.getId());
        dto.setEnquiryNo(enquiry.getEnquiryNo());
        if (enquiry.getPatient() != null) {
            dto.setPatientId(enquiry.getPatient().getId());
            dto.setPatientUhid(enquiry.getPatient().getUhid());
            dto.setPatientName(enquiry.getPatient().getFullName());
        }
        if (enquiry.getDepartment() != null) {
            dto.setDepartmentId(enquiry.getDepartment().getId());
            dto.setDepartmentName(enquiry.getDepartment().getName());
        }
        dto.setCategory(enquiry.getCategory());
        dto.setPriority(enquiry.getPriority());
        dto.setStatus(enquiry.getStatus());
        dto.setSubject(enquiry.getSubject());
        dto.setDescription(enquiry.getDescription());
        dto.setResolution(enquiry.getResolution());
        dto.setAssignedToUser(enquiry.getAssignedToUser());
        dto.setEnquirerName(enquiry.getEnquirerName());
        dto.setPhone(enquiry.getPhone());
        dto.setEmail(enquiry.getEmail());
        dto.setCreatedAt(enquiry.getCreatedAt());
        dto.setUpdatedAt(enquiry.getUpdatedAt());
        dto.setResolvedAt(enquiry.getResolvedAt());
        if (includeAuditLogs) {
            dto.setAuditLogs(auditLogRepository.findByEnquiryIdOrderByEventAtDesc(enquiry.getId()).stream()
                    .map(this::toAuditDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private EnquiryAuditLogDto toAuditDto(EnquiryAuditLog log) {
        EnquiryAuditLogDto dto = new EnquiryAuditLogDto();
        dto.setId(log.getId());
        dto.setEventType(log.getEventType());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setEventAt(log.getEventAt());
        dto.setNote(log.getNote());
        return dto;
    }

    private void audit(Long enquiryId, EnquiryAuditEventType eventType, String note) {
        EnquiryAuditLog log = new EnquiryAuditLog();
        log.setEnquiryId(enquiryId);
        log.setEventType(eventType);
        log.setEventAt(Instant.now());
        log.setNote(blankToNull(note));
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .filter(name -> name != null && !name.isBlank())
                .ifPresent(log::setPerformedBy);
        auditLogRepository.save(log);
    }

    private EnquiryAuditEventType eventForStatus(EnquiryStatus status) {
        return switch (status) {
            case RESOLVED -> EnquiryAuditEventType.RESOLVED;
            case CLOSED -> EnquiryAuditEventType.CLOSED;
            case ESCALATED -> EnquiryAuditEventType.ESCALATED;
            default -> EnquiryAuditEventType.STATUS_CHANGED;
        };
    }

    private String generateEnquiryNo() {
        String date = ENQUIRY_NO_DATE.format(LocalDate.now());
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ENQ-" + date + "-" + suffix;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeQuery(String value) {
        String normalized = blankToNull(value);
        return normalized != null ? normalized.toLowerCase(Locale.ROOT) : null;
    }
}
