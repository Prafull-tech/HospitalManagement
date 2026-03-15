package com.hospital.hms.dashboard.service;

import com.hospital.hms.billing.repository.PaymentRepository;
import com.hospital.hms.dashboard.dto.DashboardStatsDto;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregates hospital statistics for dashboard and print.
 */
@Service
public class DashboardStatsService {

    private static final List<AdmissionStatus> ACTIVE_ADMISSION_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final PatientRepository patientRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final IPDAdmissionRepository ipdAdmissionRepository;
    private final PaymentRepository paymentRepository;

    public DashboardStatsService(PatientRepository patientRepository,
                                 OPDVisitRepository opdVisitRepository,
                                 IPDAdmissionRepository ipdAdmissionRepository,
                                 PaymentRepository paymentRepository) {
        this.patientRepository = patientRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null) fromDate = LocalDate.now();
        if (toDate == null) toDate = LocalDate.now();
        if (fromDate.isAfter(toDate)) {
            LocalDate t = fromDate;
            fromDate = toDate;
            toDate = t;
        }
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        long totalPatientsRegistered = patientRepository.countByRegistrationDateBetween(start, end);
        long totalOPDVisits = opdVisitRepository.countByVisitDateBetween(fromDate, toDate);
        long totalAdmitted = ipdAdmissionRepository.countByAdmissionDateTimeBetween(start, end);
        long totalDischarged = ipdAdmissionRepository.countByDischargeDateTimeBetween(start, end);
        long totalCurrentlyAdmitted = ipdAdmissionRepository.countByAdmissionStatusIn(ACTIVE_ADMISSION_STATUSES);

        ZoneId zone = ZoneId.systemDefault();
        java.time.Instant instantFrom = ZonedDateTime.of(start, zone).toInstant();
        java.time.Instant instantTo = ZonedDateTime.of(end, zone).toInstant();
        double totalCollection = paymentRepository.sumAmountByCreatedAtBetween(instantFrom, instantTo).doubleValue();

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setFromDate(fromDate);
        dto.setToDate(toDate);
        dto.setTotalPatientsRegistered(totalPatientsRegistered);
        dto.setTotalOPDVisits(totalOPDVisits);
        dto.setTotalAdmitted(totalAdmitted);
        dto.setTotalDischarged(totalDischarged);
        dto.setTotalCurrentlyAdmitted(totalCurrentlyAdmitted);
        dto.setTotalCollection(totalCollection);
        return dto;
    }
}
