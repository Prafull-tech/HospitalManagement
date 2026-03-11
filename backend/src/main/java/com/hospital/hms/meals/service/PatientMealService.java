package com.hospital.hms.meals.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.meals.dto.PatientMealResponseDto;
import com.hospital.hms.meals.dto.ServeMealRequestDto;
import com.hospital.hms.meals.entity.PatientMeal;
import com.hospital.hms.meals.entity.PatientMealStatus;
import com.hospital.hms.meals.repository.PatientMealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Patient meal management: list today's meals, mark meal as served.
 */
@Service
public class PatientMealService {

    private static final Logger log = LoggerFactory.getLogger(PatientMealService.class);

    private final PatientMealRepository patientMealRepository;

    public PatientMealService(PatientMealRepository patientMealRepository) {
        this.patientMealRepository = patientMealRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientMealResponseDto> listTodayMeals() {
        LocalDate today = LocalDate.now();
        List<PatientMeal> meals = patientMealRepository.findByMealDateOrderByCreatedAtAsc(today);
        return meals.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public PatientMealResponseDto serveMeal(ServeMealRequestDto request) {
        PatientMeal meal = patientMealRepository.findById(request.getMealId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient meal not found: " + request.getMealId()));

        meal.setStatus(PatientMealStatus.SERVED);
        meal.setDeliveredBy(SecurityContextUserResolver.resolveUserId());
        meal.setDeliveredAt(java.time.Instant.now());
        meal = patientMealRepository.save(meal);

        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Patient meal served id={} patientId={} mealType={} by {}", meal.getId(), meal.getPatientId(), meal.getMealType(), user);
        return toDto(meal);
    }

    private PatientMealResponseDto toDto(PatientMeal meal) {
        PatientMealResponseDto dto = new PatientMealResponseDto();
        dto.setId(meal.getId());
        dto.setPatientId(meal.getPatientId());
        dto.setIpdAdmissionId(meal.getIpdAdmissionId());
        dto.setMealType(meal.getMealType());
        dto.setDietType(meal.getDietType());
        dto.setDeliveredBy(meal.getDeliveredBy());
        dto.setDeliveredAt(meal.getDeliveredAt());
        dto.setStatus(meal.getStatus());
        dto.setMealDate(meal.getMealDate());
        return dto;
    }
}
