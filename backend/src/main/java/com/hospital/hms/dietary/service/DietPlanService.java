package com.hospital.hms.dietary.service;

import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.dietary.dto.DietPlanRequestDto;
import com.hospital.hms.dietary.dto.DietPlanResponseDto;
import com.hospital.hms.dietary.entity.DietPlan;
import com.hospital.hms.dietary.repository.DietPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Diet plan management: list (with active filter), create.
 */
@Service
public class DietPlanService {

    private static final Logger log = LoggerFactory.getLogger(DietPlanService.class);

    private final DietPlanRepository dietPlanRepository;

    public DietPlanService(DietPlanRepository dietPlanRepository) {
        this.dietPlanRepository = dietPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<DietPlanResponseDto> listPlans(Boolean active) {
        List<DietPlan> plans = active != null
                ? dietPlanRepository.findByActiveOrderByCreatedAtDesc(active)
                : dietPlanRepository.findAllByOrderByCreatedAtDesc();
        return plans.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public DietPlanResponseDto createPlan(DietPlanRequestDto request) {
        DietPlan plan = new DietPlan();
        plan.setPatientId(request.getPatientId());
        plan.setIpdAdmissionId(request.getIpdAdmissionId());
        plan.setDietType(request.getDietType());
        plan.setMealSchedule(request.getMealSchedule());
        plan.setCreatedByDoctor(request.getCreatedByDoctor());
        plan.setActive(request.getActive() != null ? request.getActive() : true);

        plan = dietPlanRepository.save(plan);
        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Diet plan created id={} patientId={} dietType={} by {}", plan.getId(), plan.getPatientId(), plan.getDietType(), user);
        return toDto(plan);
    }

    private DietPlanResponseDto toDto(DietPlan plan) {
        DietPlanResponseDto dto = new DietPlanResponseDto();
        dto.setId(plan.getId());
        dto.setPatientId(plan.getPatientId());
        dto.setIpdAdmissionId(plan.getIpdAdmissionId());
        dto.setDietType(plan.getDietType());
        dto.setMealSchedule(plan.getMealSchedule());
        dto.setCreatedByDoctor(plan.getCreatedByDoctor());
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }
}
