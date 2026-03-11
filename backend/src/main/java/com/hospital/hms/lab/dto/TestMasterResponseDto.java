package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.PriorityLevel;
import com.hospital.hms.lab.entity.SampleType;
import com.hospital.hms.lab.entity.TestCategory;

import java.math.BigDecimal;

/**
 * Response DTO for TestMaster.
 */
public class TestMasterResponseDto {

    private Long id;
    private String testCode;
    private String testName;
    private TestCategory category;
    private SampleType sampleType;
    private Integer normalTATMinutes;
    private BigDecimal price;
    private Boolean active;
    private PriorityLevel priorityLevel;
    private Boolean isPanel;
    private String panelTestCodes;
    private String description;
    private String normalRange;
    private String unit;
    private String instructions;
    private String createdByUser;
    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;

    public TestMasterResponseDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public TestCategory getCategory() {
        return category;
    }

    public void setCategory(TestCategory category) {
        this.category = category;
    }

    public SampleType getSampleType() {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Integer getNormalTATMinutes() {
        return normalTATMinutes;
    }

    public void setNormalTATMinutes(Integer normalTATMinutes) {
        this.normalTATMinutes = normalTATMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Boolean getIsPanel() {
        return isPanel;
    }

    public void setIsPanel(Boolean isPanel) {
        this.isPanel = isPanel;
    }

    public String getPanelTestCodes() {
        return panelTestCodes;
    }

    public void setPanelTestCodes(String panelTestCodes) {
        this.panelTestCodes = panelTestCodes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
