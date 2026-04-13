package com.hospital.hms.appointment.service;

import com.hospital.hms.appointment.dto.*;
import com.hospital.hms.appointment.entity.*;
import com.hospital.hms.appointment.repository.AppointmentAuditLogRepository;
import com.hospital.hms.appointment.repository.AppointmentRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.opd.dto.OPDVisitRequestDto;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.service.OPDVisitService;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OPDVisitService opdVisitService;
    private final TenantContextService tenantContextService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AppointmentAuditLogRepository auditLogRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              OPDVisitService opdVisitService,
                              TenantContextService tenantContextService) {
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.opdVisitService = opdVisitService;
        this.tenantContextService = tenantContextService;
    }

    @Transactional(readOnly = true)
    public AppointmentDashboardDto getDashboard(LocalDate date) {
        if (date == null) date = LocalDate.now();
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        AppointmentDashboardDto dto = new AppointmentDashboardDto();
        dto.setDate(date);

        List<AppointmentStatus> todayStatuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_CONFIRMATION,
                AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW);
        List<Appointment> todayList = appointmentRepository.findByAppointmentDateAndStatusInWithAssociations(hospitalId, date, todayStatuses);

        dto.setTotalAppointmentsToday(todayList.size());
        dto.setWalkIns(appointmentRepository.countByHospitalIdAndAppointmentDateAndSource(hospitalId, date, AppointmentSource.WALK_IN));
        dto.setOnlineBookings(appointmentRepository.countByHospitalIdAndAppointmentDateAndSource(hospitalId, date, AppointmentSource.ONLINE));
        dto.setCompletedConsultations(appointmentRepository.countByHospitalIdAndAppointmentDateAndStatus(hospitalId, date, AppointmentStatus.COMPLETED));
        dto.setCancelled(appointmentRepository.countByHospitalIdAndAppointmentDateAndStatus(hospitalId, date, AppointmentStatus.CANCELLED));
        dto.setNoShow(appointmentRepository.countByHospitalIdAndAppointmentDateAndStatus(hospitalId, date, AppointmentStatus.NO_SHOW));

        dto.setTodaysAppointments(todayList.stream().map(this::toResponse).collect(Collectors.toList()));

        LocalDate tomorrow = date.plusDays(1);
        List<Appointment> upcoming = appointmentRepository.findByAppointmentDateAndStatusInWithAssociations(
            hospitalId, tomorrow, List.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING_CONFIRMATION));
        dto.setUpcomingAppointments(upcoming.stream().map(this::toResponse).collect(Collectors.toList()));

        List<Appointment> cancelledList = appointmentRepository.findByHospitalIdAndAppointmentDateAndStatusInOrderBySlotTimeAsc(
            hospitalId, date, List.of(AppointmentStatus.CANCELLED));
        dto.setCancelledAppointments(cancelledList.stream().map(this::toResponse).collect(Collectors.toList()));

        List<Appointment> noShowList = appointmentRepository.findByAppointmentDateAndStatusInWithAssociations(
            hospitalId, date, List.of(AppointmentStatus.NO_SHOW));
        dto.setNoShowPatients(noShowList.stream().map(this::toResponse).collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public AppointmentResponseDto create(AppointmentRequestDto request, String createdBy) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        Patient patient = patientRepository.findByIdAndHospitalId(request.getPatientId(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        Doctor doctor = doctorRepository.findByIdWithDepartmentAndHospitalId(request.getDoctorId(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        validateNoSlotConflict(hospital.getId(), doctor.getId(), request.getAppointmentDate(), request.getSlotTime(), null);

        int nextToken = getNextTokenForDoctorAndDate(hospital.getId(), doctor.getId(), request.getAppointmentDate());

        Appointment a = new Appointment();
        a.setHospital(hospital);
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setDepartment(doctor.getDepartment());
        a.setAppointmentDate(request.getAppointmentDate());
        a.setSlotTime(request.getSlotTime());
        a.setTokenNo(nextToken);
        a.setStatus(AppointmentStatus.BOOKED);
        a.setSource(AppointmentSource.FRONT_DESK);
        a.setVisitType(request.getVisitType());
        a.setCreatedBy(createdBy);
        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.CREATED, createdBy, null);
        return toResponse(a);
    }

    @Transactional
    public AppointmentResponseDto createWalkIn(WalkInAppointmentRequestDto request, String createdBy) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        Patient patient = patientRepository.findByUhidAndHospitalId(request.getPatientUhid().trim(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + request.getPatientUhid()));
        Doctor doctor = doctorRepository.findByIdWithDepartmentAndHospitalId(request.getDoctorId(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        LocalDate date = request.getAppointmentDate() != null ? request.getAppointmentDate() : LocalDate.now();
        java.time.LocalTime slot = request.getSlotTime() != null ? request.getSlotTime() : java.time.LocalTime.now();

        validateNoSlotConflict(hospital.getId(), doctor.getId(), date, slot, null);

        int nextToken = getNextTokenForDoctorAndDate(hospital.getId(), doctor.getId(), date);

        Appointment a = new Appointment();
        a.setHospital(hospital);
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setDepartment(doctor.getDepartment());
        a.setAppointmentDate(date);
        a.setSlotTime(slot);
        a.setTokenNo(nextToken);
        a.setStatus(AppointmentStatus.BOOKED);
        a.setSource(AppointmentSource.WALK_IN);
        a.setCreatedBy(createdBy);
        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.CREATED, createdBy, "Walk-in");
        return toResponse(a);
    }

    @Transactional
    public AppointmentResponseDto createOnline(OnlineAppointmentRequestDto request) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        Patient patient = patientRepository.findByUhidAndHospitalId(request.getPatientUhid().trim(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + request.getPatientUhid()));
        Doctor doctor = doctorRepository.findByIdWithDepartmentAndHospitalId(request.getDoctorId(), hospital.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        validateNoSlotConflict(hospital.getId(), doctor.getId(), request.getAppointmentDate(), request.getSlotTime(), null);

        int nextToken = getNextTokenForDoctorAndDate(hospital.getId(), doctor.getId(), request.getAppointmentDate());

        Appointment a = new Appointment();
        a.setHospital(hospital);
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setDepartment(doctor.getDepartment());
        a.setAppointmentDate(request.getAppointmentDate());
        a.setSlotTime(request.getSlotTime());
        a.setTokenNo(nextToken);
        a.setStatus(AppointmentStatus.PENDING_CONFIRMATION);
        a.setSource(AppointmentSource.ONLINE);
        a.setVisitType(request.getVisitType());
        a.setCreatedBy("ONLINE");
        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.CREATED, "ONLINE", null);
        return toResponse(a);
    }

    @Transactional
    public AppointmentResponseDto reschedule(Long id, RescheduleRequestDto request, String userId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot reschedule cancelled or completed appointment");
        }

        if (request.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findByIdWithDepartmentAndHospitalId(request.getDoctorId(), hospitalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));
            a.setDoctor(doctor);
            a.setDepartment(doctor.getDepartment());
        }
        if (request.getAppointmentDate() != null) a.setAppointmentDate(request.getAppointmentDate());
        if (request.getSlotTime() != null) a.setSlotTime(request.getSlotTime());

        // Validate slot conflict with the updated values
        validateNoSlotConflict(hospitalId, a.getDoctor().getId(), a.getAppointmentDate(), a.getSlotTime(), a.getId());

        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.RESCHEDULED, userId, null);
        return toResponse(a);
    }

    @Transactional
    public AppointmentResponseDto cancel(Long id, CancelRequestDto request, String userId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setCancelReason(request != null && request.getReason() != null ? request.getReason().trim() : null);
        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.CANCELLED, userId, a.getCancelReason());
        return toResponse(a);
    }

    @Transactional
    public AppointmentResponseDto markNoShow(Long id, String userId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setStatus(AppointmentStatus.NO_SHOW);
        a = appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.NO_SHOW, userId, null);
        return toResponse(a);
    }

    @Transactional
    public OPDVisitResponseDto convertToOpdVisit(Long appointmentId, String userId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(appointmentId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));
        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalArgumentException("Cannot convert cancelled or no-show appointment to OPD visit");
        }

        OPDVisitRequestDto opdReq = new OPDVisitRequestDto();
        opdReq.setPatientUhid(a.getPatient().getUhid());
        opdReq.setDoctorId(a.getDoctor().getId());
        opdReq.setVisitDate(a.getAppointmentDate());

        OPDVisitResponseDto created = opdVisitService.registerVisit(opdReq);
        a.setStatus(AppointmentStatus.COMPLETED);
        a.setOpdVisitId(created.getId());
        appointmentRepository.save(a);

        logAudit(a.getId(), AppointmentAuditEventType.CONVERTED_TO_OPD, userId, "OPD visit ID: " + created.getId());
        return created;
    }

    @Transactional
    public AppointmentResponseDto confirmOnline(Long id, String userId) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        if (a.getStatus() != AppointmentStatus.PENDING_CONFIRMATION) {
            throw new IllegalArgumentException("Only PENDING_CONFIRMATION appointments can be confirmed");
        }
        a.setStatus(AppointmentStatus.CONFIRMED);
        a = appointmentRepository.save(a);
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> search(LocalDate date, Long doctorId, AppointmentStatus status,
                                                       String patientUhid, String patientName, int page, int size) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appointmentDate", "slotTime"));
        Page<Appointment> result = appointmentRepository.search(hospitalId, date, doctorId, status, patientUhid, patientName, pageable);
        return result.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getQueue(Long doctorId, LocalDate date) {
        if (date == null) date = LocalDate.now();
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        return appointmentRepository.findQueueWithAssociations(hospitalId, date, doctorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(Long id) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Appointment a = appointmentRepository.findByIdWithAssociations(id, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        return toResponse(a);
    }

    private void validateNoSlotConflict(Long hospitalId, Long doctorId, LocalDate date, java.time.LocalTime slotTime, Long excludeId) {
        boolean conflict;
        if (excludeId != null) {
            conflict = appointmentRepository.existsActiveByDoctorAndDateAndSlotExcluding(hospitalId, doctorId, date, slotTime, excludeId);
        } else {
            conflict = appointmentRepository.existsActiveByDoctorAndDateAndSlot(hospitalId, doctorId, date, slotTime);
        }
        if (conflict) {
            throw new IllegalArgumentException(
                    "This doctor already has an appointment booked at " + slotTime + " on " + date + ". Please choose a different time slot.");
        }
    }

    private int getNextTokenForDoctorAndDate(Long hospitalId, Long doctorId, LocalDate date) {
        List<Appointment> existing = appointmentRepository.findByHospitalIdAndAppointmentDateAndDoctorIdOrderBySlotTimeAscTokenNoAsc(hospitalId, date, doctorId);
        int max = existing.stream()
                .mapToInt(a -> a.getTokenNo() != null ? a.getTokenNo() : 0)
                .max()
                .orElse(0);
        return max + 1;
    }

    private void logAudit(Long appointmentId, AppointmentAuditEventType eventType, String userId, String remarks) {
        AppointmentAuditLog log = new AppointmentAuditLog();
        log.setAppointmentId(appointmentId);
        log.setEventType(eventType);
        log.setUserId(userId);
        log.setRemarks(remarks);
        auditLogRepository.save(log);
    }

    private AppointmentResponseDto toResponse(Appointment a) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(a.getId());
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientUhid(a.getPatient().getUhid());
        dto.setPatientName(a.getPatient().getFullName());
        dto.setDoctorId(a.getDoctor().getId());
        dto.setDoctorName(a.getDoctor().getFullName());
        dto.setDoctorCode(a.getDoctor().getCode());
        dto.setDepartmentId(a.getDepartment().getId());
        dto.setDepartmentName(a.getDepartment().getName());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setSlotTime(a.getSlotTime());
        dto.setTokenNo(a.getTokenNo());
        dto.setStatus(a.getStatus());
        dto.setSource(a.getSource());
        dto.setVisitType(a.getVisitType());
        dto.setCreatedBy(a.getCreatedBy());
        dto.setCancelReason(a.getCancelReason());
        dto.setOpdVisitId(a.getOpdVisitId());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
