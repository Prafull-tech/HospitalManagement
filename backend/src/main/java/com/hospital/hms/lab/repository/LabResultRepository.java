package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LabResult entity.
 */
@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Long> {

    List<LabResult> findByTestOrderIdOrderByParameterNameAsc(Long testOrderId);

    List<LabResult> findByTestOrderIdAndIsCriticalTrue(Long testOrderId);
}
