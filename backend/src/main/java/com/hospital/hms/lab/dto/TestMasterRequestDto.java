package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.PriorityLevel;
import com.hospital.hms.lab.entity.SampleType;
import com.hospital.hms.lab.entity.TestCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating TestMaster.
 */
public class TestMasterRequestDto {

    @NotBlank(message = "Test code is required")
    @Size(max = 50)
    private String testCode;

    @NotBlank(message = "Test name is required")
    @Size(max = 255)
    private String testName;

    @NotNull(message = "Category is required")
    private TestCategory category;

    @NotNull(message = "Sample type is required")
    private SampleType sampleType;

    @NotNull(message = "Normal TAT (minutes) is required")
    @Positive
    private Integer normalTATMinutes;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private Boolean active = true;

    private PriorityLevel priorityLevel = PriorityLevel.ROUTINE;

    private Boolean isPanel = false;

    @Size(max = 1000)
    private String panelTestCodes; // Comma-separated test codes if panel

    @Size(max = 500)
    private String description;

    @Size(max = 1000)
    private String normalRange;

    @Size(max = 500)
    private String instructions;

    public TestMasterRequestDto() {
    }

    // Getters and setters
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
