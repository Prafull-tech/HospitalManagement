package com.hospital.hms.dietary.repository;

import com.hospital.hms.dietary.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByActiveOrderByCreatedAtDesc(boolean active);

    List<DietPlan> findAllByOrderByCreatedAtDesc();
}
