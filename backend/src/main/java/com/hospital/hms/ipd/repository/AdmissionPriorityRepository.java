package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.AdmissionPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Admission Priority master. DB-agnostic (H2 & MySQL).
 */
public interface AdmissionPriorityRepository extends JpaRepository<AdmissionPriority, Long> {

    List<AdmissionPriority> findAllByOrderByPriorityOrderAsc();

    List<AdmissionPriority> findByActiveTrueOrderByPriorityOrderAsc();
}
