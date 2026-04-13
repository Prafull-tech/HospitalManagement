package com.hospital.hms.superadmin.repository;

import com.hospital.hms.superadmin.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findByIsActiveTrueOrderByPlanNameAsc();

    List<SubscriptionPlan> findAllByOrderByPlanNameAsc();

    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);

    boolean existsByPlanCodeAndIdNot(String planCode, Long excludeId);
}
