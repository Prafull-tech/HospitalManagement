package com.hospital.hms.prescription.repository;

import com.hospital.hms.prescription.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "opdVisit", "ipdAdmission", "items"})
    Optional<Prescription> findByIdAndHospitalId(Long id, Long hospitalId);

       Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    Optional<Prescription> findByPrescriptionNumberAndHospitalId(String prescriptionNumber, Long hospitalId);

    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "opdVisit", "ipdAdmission", "items"})
    List<Prescription> findByPatientIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(Long patientId, Long hospitalId);

    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "opdVisit", "ipdAdmission", "items"})
    List<Prescription> findByDoctorIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(Long doctorId, Long hospitalId);

    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "opdVisit", "ipdAdmission", "items"})
    List<Prescription> findByOpdVisitIdAndHospitalIdOrderByPrescriptionDateDescIdDesc(Long opdVisitId, Long hospitalId);

    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "opdVisit", "ipdAdmission"})
    @Query("SELECT p FROM Prescription p WHERE p.hospital.id = :hospitalId " +
           "AND (:patientId IS NULL OR p.patient.id = :patientId) " +
           "AND (:doctorId IS NULL OR p.doctor.id = :doctorId) " +
           "AND (:opdVisitId IS NULL OR p.opdVisit.id = :opdVisitId) " +
           "AND (:fromDate IS NULL OR p.prescriptionDate >= :fromDate) " +
           "AND (:toDate IS NULL OR p.prescriptionDate <= :toDate)")
    Page<Prescription> search(@Param("hospitalId") Long hospitalId,
                              @Param("patientId") Long patientId,
                              @Param("doctorId") Long doctorId,
                              @Param("opdVisitId") Long opdVisitId,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate,
                              Pageable pageable);
}