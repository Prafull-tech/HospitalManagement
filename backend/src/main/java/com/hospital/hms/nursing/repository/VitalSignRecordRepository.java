package com.hospital.hms.nursing.repository;

import com.hospital.hms.nursing.entity.VitalSignRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Vital sign record repository. DB-agnostic.
 */
public interface VitalSignRecordRepository extends JpaRepository<VitalSignRecord, Long> {

    List<VitalSignRecord> findByIpdAdmissionIdOrderByRecordedAtDesc(Long ipdAdmissionId);
}
