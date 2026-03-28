package com.hospital.hms.reception.repository;

import com.hospital.hms.reception.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Patient repository. Derived queries only; no native SQL for DB-agnostic behaviour.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUhid(String uhid);

    Optional<Patient> findByUhidAndActiveTrue(String uhid);

    Optional<Patient> findByRegistrationNumber(String registrationNumber);

    /** Returns all patients with this phone (multiple allowed). */
    List<Patient> findByPhone(String phone);

    List<Patient> findByPhoneAndActiveTrue(String phone);

    List<Patient> findByFullNameContainingIgnoreCase(String name);

    List<Patient> findByFullNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Patient> findByIdProofNumber(String idProofNumber);

    List<Patient> findByIdProofNumberAndActiveTrue(String idProofNumber);

    Page<Patient> findAllByActiveTrue(Pageable pageable);

    long countByRegistrationDateBetween(LocalDateTime start, LocalDateTime end);
}
