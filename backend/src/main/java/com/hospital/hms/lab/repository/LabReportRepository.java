package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabReport;
import com.hospital.hms.lab.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT r FROM LabReport r JOIN r.testOrder o JOIN o.patient p WHERE p.id = :patientId ORDER BY r.releasedAt DESC NULLS LAST, r.generatedAt DESC")
    List<LabReport> findByTestOrder_Patient_IdOrderByReleasedAtDesc(@Param("patientId") Long patientId);

    @Query("SELECT r FROM LabReport r JOIN r.testOrder o JOIN o.patient p JOIN o.testMaster tm " +
            "WHERE (:uhid IS NULL OR :uhid = '' OR LOWER(p.uhid) LIKE LOWER(CONCAT('%', :uhid, '%'))) " +
            "AND (:patientName IS NULL OR :patientName = '' OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
            "AND (:testName IS NULL OR :testName = '' OR LOWER(tm.testName) LIKE LOWER(CONCAT('%', :testName, '%'))) " +
            "AND (:fromDate IS NULL OR COALESCE(r.releasedAt, r.generatedAt) >= :fromDate) " +
            "AND (:toDate IS NULL OR COALESCE(r.releasedAt, r.generatedAt) <= :toDate) " +
            "ORDER BY COALESCE(r.releasedAt, r.generatedAt) DESC NULLS LAST")
    List<LabReport> searchReports(
            @Param("uhid") String uhid,
            @Param("patientName") String patientName,
            @Param("testName") String testName,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
