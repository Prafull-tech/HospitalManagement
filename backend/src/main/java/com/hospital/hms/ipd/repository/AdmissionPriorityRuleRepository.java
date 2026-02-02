package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.AdmissionConditionType;
import com.hospital.hms.ipd.entity.AdmissionPriorityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Admission priority rules: condition type → P1–P4 mapping.
 */
public interface AdmissionPriorityRuleRepository extends JpaRepository<AdmissionPriorityRule, Long> {

    Optional<AdmissionPriorityRule> findByConditionTypeAndActiveTrue(AdmissionConditionType conditionType);

    List<AdmissionPriorityRule> findAllByActiveTrue();
}
