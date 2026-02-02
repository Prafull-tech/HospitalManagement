package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionCategory;
import com.hospital.hms.ipd.entity.PriorityCode;

/**
 * Response DTO for a single admission priority master record (GET /api/admission-priority).
 * DB-agnostic.
 */
public class AdmissionPriorityItemDto {

    private Long id;
    private PriorityCode priorityCode;
    private AdmissionCategory category;
    private String description;
    private Integer priorityOrder;
    private Boolean active;

    public AdmissionPriorityItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PriorityCode getPriorityCode() {
        return priorityCode;
    }

    public void setPriorityCode(PriorityCode priorityCode) {
        this.priorityCode = priorityCode;
    }

    public AdmissionCategory getCategory() {
        return category;
    }

    public void setCategory(AdmissionCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
