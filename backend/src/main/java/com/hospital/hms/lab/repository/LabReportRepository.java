package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabReport;
import com.hospital.hms.lab.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LabReport entity.
 */
@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {

    Optional<LabReport> findByReportNumber(String reportNumber);

    Optional<LabReport> findByTestOrderId(Long testOrderId);

    List<LabReport> findByStatusOrderByGeneratedAtDesc(ReportStatus status);

    List<LabReport> findByTestOrderPatientIdOrderByReleasedAtDesc(Long patientId);
}
