package com.hospital.hms.reception.repository;

import com.hospital.hms.reception.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Patient repository. Derived queries only; no native SQL for DB-agnostic behaviour.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUhid(String uhid);

    Optional<Patient> findByRegistrationNumber(String registrationNumber);

    Optional<Patient> findByPhone(String phone);

    List<Patient> findByFullNameContainingIgnoreCase(String name);

    long countByRegistrationDateBetween(LocalDateTime start, LocalDateTime end);
}
