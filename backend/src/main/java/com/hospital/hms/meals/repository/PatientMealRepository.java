package com.hospital.hms.meals.repository;

import com.hospital.hms.meals.entity.PatientMeal;
import com.hospital.hms.meals.entity.PatientMealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PatientMealRepository extends JpaRepository<PatientMeal, Long> {

    List<PatientMeal> findByMealDateOrderByCreatedAtAsc(LocalDate mealDate);
}
