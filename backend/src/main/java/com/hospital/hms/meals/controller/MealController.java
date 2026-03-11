package com.hospital.hms.meals.controller;

import com.hospital.hms.meals.dto.PatientMealResponseDto;
import com.hospital.hms.meals.dto.ServeMealRequestDto;
import com.hospital.hms.meals.service.PatientMealService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient meals API. Base path: /api (context) + /meals (mapping).
 * <ul>
 *   <li>GET  /api/meals/today — List today's meals</li>
 *   <li>POST /api/meals/serve — Mark meal as served</li>
 * </ul>
 */
@RestController
@RequestMapping("/meals")
public class MealController {

    private final PatientMealService patientMealService;

    public MealController(PatientMealService patientMealService) {
        this.patientMealService = patientMealService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<PatientMealResponseDto>> listTodayMeals() {
        List<PatientMealResponseDto> meals = patientMealService.listTodayMeals();
        return ResponseEntity.ok(meals);
    }

    @PostMapping("/serve")
    public ResponseEntity<PatientMealResponseDto> serveMeal(@Valid @RequestBody ServeMealRequestDto request) {
        PatientMealResponseDto served = patientMealService.serveMeal(request);
        return ResponseEntity.ok(served);
    }
}
