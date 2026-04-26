package com.hospital.hms.doctor.service;

import com.hospital.hms.appointment.dto.AppointmentResponseDto;
import com.hospital.hms.appointment.entity.Appointment;
import com.hospital.hms.appointment.repository.AppointmentRepository;
import com.hospital.hms.doctor.dto.DoctorDashboardDto;
import com.hospital.hms.lab.entity.LabOrderStatus;
import com.hospital.hms.lab.repository.LabOrderRepository;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.entity.VisitStatus;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorDashboardService {

    private final AppointmentRepository appointmentRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final LabOrderRepository labOrderRepository;
    private final TenantContextService tenantContextService;

    public DoctorDashboardService(AppointmentRepository appointmentRepository,
                                  OPDVisitRepository opdVisitRepository,
                                  LabOrderRepository labOrderRepository,
                                  TenantContextService tenantContextService) {
        this.appointmentRepository = appointmentRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.labOrderRepository = labOrderRepository;
        this.tenantContextService = tenantContextService;
    }

    @Transactional(readOnly = true)
    public DoctorDashboardDto getDashboard(Long doctorId, LocalDate date) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate selectedDate = date != null ? date : LocalDate.now();

        List<Appointment> appointments = appointmentRepository.findQueueWithAssociations(hospitalId, selectedDate, doctorId);
        List<OPDVisit> queue = opdVisitRepository.findByHospitalIdAndVisitDateAndDoctorId(hospitalId, selectedDate, doctorId);
        long completedConsultations = opdVisitRepository.countByHospitalIdAndVisitDateAndDoctorIdAndVisitStatus(hospitalId, selectedDate, doctorId, VisitStatus.COMPLETED);
        long pendingLabReports = labOrderRepository.countPendingByDoctorIdAndHospitalId(hospitalId, doctorId, Arrays.asList(LabOrderStatus.COMPLETED, LabOrderStatus.CANCELLED));
        List<OPDVisit> recentVisits = opdVisitRepository.search(
                hospitalId,
                null,
                doctorId,
                null,
                null,
                null,
            null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "visitDate", "id")))
            .getContent();

        DoctorDashboardDto dto = new DoctorDashboardDto();
        dto.setTodayAppointments(appointments.size());
        dto.setWaitingPatients(queue.stream().filter(visit -> visit.getVisitStatus() == VisitStatus.REGISTERED).count());
        dto.setCompletedConsultations(completedConsultations);
        dto.setPendingLabReports(pendingLabReports);
        dto.setRecentPatients(recentVisits.stream().map(this::toVisitResponse).collect(Collectors.toList()));
        dto.setTodayQueue(queue.stream().map(this::toVisitResponse).collect(Collectors.toList()));
        dto.setUpcomingAppointments(appointments.stream().map(this::toAppointmentResponse).collect(Collectors.toList()));
        return dto;
    }

    private OPDVisitResponseDto toVisitResponse(OPDVisit visit) {
        OPDVisitResponseDto dto = new OPDVisitResponseDto();
        dto.setId(visit.getId());
        dto.setVisitNumber(visit.getVisitNumber());
        dto.setPatientId(visit.getPatient().getId());
        dto.setPatientUhid(visit.getPatient().getUhid());
        dto.setPatientName(visit.getPatient().getFullName());
        dto.setDoctorId(visit.getDoctor().getId());
        dto.setDoctorName(visit.getDoctor().getFullName());
        dto.setDoctorCode(visit.getDoctor().getCode());
        dto.setDepartmentId(visit.getDepartment().getId());
        dto.setDepartmentName(visit.getDepartment().getName());
        dto.setVisitDate(visit.getVisitDate());
        dto.setVisitStatus(visit.getVisitStatus());
        dto.setTokenNumber(visit.getTokenNumber());
        dto.setReferredToDepartmentId(visit.getReferredToDepartmentId());
        dto.setReferredToDoctorId(visit.getReferredToDoctorId());
        dto.setReferToIpd(visit.getReferToIpd());
        dto.setReferralRemarks(visit.getReferralRemarks());
        dto.setVisitType(visit.getVisitType());
        dto.setConsultationOutcome(visit.getConsultationOutcome());
        dto.setAdmissionRecommended(visit.getAdmissionRecommended());
        dto.setAdmissionRecommendedAt(visit.getAdmissionRecommendedAt());
        dto.setAdmissionRecommendedBy(visit.getAdmissionRecommendedBy());
        dto.setCreatedAt(visit.getCreatedAt());
        dto.setUpdatedAt(visit.getUpdatedAt());
        return dto;
    }

    private AppointmentResponseDto toAppointmentResponse(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientUhid(appointment.getPatient().getUhid());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        dto.setDoctorCode(appointment.getDoctor().getCode());
        dto.setDepartmentId(appointment.getDepartment().getId());
        dto.setDepartmentName(appointment.getDepartment().getName());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setSlotTime(appointment.getSlotTime());
        dto.setTokenNo(appointment.getTokenNo());
        dto.setStatus(appointment.getStatus());
        dto.setSource(appointment.getSource());
        dto.setVisitType(appointment.getVisitType());
        dto.setCreatedBy(appointment.getCreatedBy());
        dto.setCancelReason(appointment.getCancelReason());
        dto.setOpdVisitId(appointment.getOpdVisitId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
}