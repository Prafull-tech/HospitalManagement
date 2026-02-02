package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IPD admission repository. JPQL only; DB-agnostic.
 */
public interface IPDAdmissionRepository extends JpaRepository<IPDAdmission, Long> {

    Optional<IPDAdmission> findByAdmissionNumber(String admissionNumber);

    @Query("SELECT a FROM IPDAdmission a WHERE a.patient.id = :patientId " +
           "AND a.admissionStatus IN :statuses")
    List<IPDAdmission> findByPatientIdAndAdmissionStatusIn(
            @Param("patientId") Long patientId,
            @Param("statuses") List<AdmissionStatus> statuses);

    List<IPDAdmission> findByAdmissionStatusIn(List<AdmissionStatus> statuses);

    @Query("SELECT a FROM IPDAdmission a WHERE " +
           "(:admissionNumber IS NULL OR a.admissionNumber = :admissionNumber) " +
           "AND (:patientUhid IS NULL OR a.patient.uhid = :patientUhid) " +
           "AND (:patientName IS NULL OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
           "AND (:status IS NULL OR a.admissionStatus = :status) " +
           "AND (:fromDate IS NULL OR a.admissionDateTime >= :fromDate) " +
           "AND (:toDate IS NULL OR a.admissionDateTime <= :toDate)")
    Page<IPDAdmission> search(@Param("admissionNumber") String admissionNumber,
                             @Param("patientUhid") String patientUhid,
                             @Param("patientName") String patientName,
                             @Param("status") AdmissionStatus status,
                             @Param("fromDate") LocalDateTime fromDate,
                             @Param("toDate") LocalDateTime toDate,
                             Pageable pageable);

    long countByAdmissionDateTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByDischargeDateTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByAdmissionStatusIn(List<AdmissionStatus> statuses);
}
