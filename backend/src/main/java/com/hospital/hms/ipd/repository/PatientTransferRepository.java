package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.PatientTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for patient transfer execution. DB-agnostic (H2 & MySQL).
 */
public interface PatientTransferRepository extends JpaRepository<PatientTransfer, Long> {

    List<PatientTransfer> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId);
}
