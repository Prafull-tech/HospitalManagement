package com.hospital.hms.nursing.repository;

import com.hospital.hms.nursing.entity.MedicationAdministration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Medication administration (MAR) repository. DB-agnostic.
 */
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    List<MedicationAdministration> findByIpdAdmissionIdOrderByAdministeredAtDesc(Long ipdAdmissionId);
}
