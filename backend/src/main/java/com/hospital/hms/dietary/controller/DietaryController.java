package com.hospital.hms.dietary.controller;

import com.hospital.hms.dietary.dto.DietPlanRequestDto;
import com.hospital.hms.dietary.dto.DietPlanResponseDto;
import com.hospital.hms.dietary.service.DietPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dietary API. Base path: /api (context) + /dietary (mapping).
 * <ul>
 *   <li>GET  /api/dietary/plans — List diet plans (optional active filter)</li>
 *   <li>POST /api/dietary/plans — Create diet plan</li>
 * </ul>
 */
@RestController
@RequestMapping("/dietary")
public class DietaryController {

    private final DietPlanService dietPlanService;

    public DietaryController(DietPlanService dietPlanService) {
        this.dietPlanService = dietPlanService;
    }

    @GetMapping("/plans")
    public ResponseEntity<List<DietPlanResponseDto>> listPlans(
            @RequestParam(required = false) Boolean active) {
        List<DietPlanResponseDto> plans = dietPlanService.listPlans(active);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/plans")
    public ResponseEntity<DietPlanResponseDto> createPlan(@Valid @RequestBody DietPlanRequestDto request) {
        DietPlanResponseDto created = dietPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
