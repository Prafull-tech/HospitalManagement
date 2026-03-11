package com.hospital.hms.meals.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for marking a meal as served (POST /api/meals/serve).
 */
public class ServeMealRequestDto {

    @NotNull(message = "Meal ID is required")
    private Long mealId;

    public ServeMealRequestDto() {
    }

    public Long getMealId() {
        return mealId;
    }

    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }
}
