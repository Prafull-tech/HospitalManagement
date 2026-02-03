package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.DoctorOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Doctor orders linked with IPD admission. Timeline and billing can extend with order events.
 */
public interface DoctorOrderRepository extends JpaRepository<DoctorOrder, Long> {

    List<DoctorOrder> findByIpdAdmissionIdOrderByOrderedAtDesc(Long ipdAdmissionId);
}
