package com.hospital.hms.opd.repository;

import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.entity.VisitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OPD visit repository. JPQL only; DB-agnostic.
 */
public interface OPDVisitRepository extends JpaRepository<OPDVisit, Long> {

    Optional<OPDVisit> findByVisitNumber(String visitNumber);

    List<OPDVisit> findByVisitDateAndDoctorIdOrderByTokenNumberAsc(LocalDate visitDate, Long doctorId);

    @Query("SELECT v FROM OPDVisit v JOIN FETCH v.patient JOIN FETCH v.doctor JOIN FETCH v.department WHERE v.id = :id")
    Optional<OPDVisit> findByIdWithAssociations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"patient", "doctor", "department"})
    @Query("SELECT v FROM OPDVisit v WHERE (:visitDate IS NULL OR v.visitDate = :visitDate) " +
           "AND (:doctorId IS NULL OR v.doctor.id = :doctorId) " +
           "AND (:status IS NULL OR v.visitStatus = :status) " +
           "AND (:patientUhid IS NULL OR v.patient.uhid = :patientUhid) " +
           "AND (:patientName IS NULL OR LOWER(v.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
           "AND (:visitNumber IS NULL OR v.visitNumber = :visitNumber)")
    Page<OPDVisit> search(@Param("visitDate") LocalDate visitDate,
                          @Param("doctorId") Long doctorId,
                          @Param("status") VisitStatus status,
                          @Param("patientUhid") String patientUhid,
                          @Param("patientName") String patientName,
                          @Param("visitNumber") String visitNumber,
                          Pageable pageable);

    long countByVisitDateBetween(LocalDate start, LocalDate end);
}
