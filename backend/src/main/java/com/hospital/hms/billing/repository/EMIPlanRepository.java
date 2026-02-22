package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.EMIPlan;
import com.hospital.hms.billing.entity.EMIPlan.EMIPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EMIPlanRepository extends JpaRepository<EMIPlan, Long> {

    List<EMIPlan> findByBillingAccountIdAndStatus(Long billingAccountId, EMIPlanStatus status);

    Optional<EMIPlan> findFirstByIpdAdmissionIdAndStatus(Long ipdAdmissionId, EMIPlanStatus status);

    boolean existsByBillingAccountIdAndStatus(Long billingAccountId, EMIPlanStatus status);
}
