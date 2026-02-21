package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Laboratory test master configuration. Defines all available tests with their
 * properties: code, name, category, sample type, TAT, price, etc.
 * Supports test panels that auto-expand into individual tests.
 */
@Entity
@Table(
    name = "lab_test_masters",
    indexes = {
        @Index(name = "idx_test_master_code", columnList = "test_code", unique = true),
        @Index(name = "idx_test_master_category", columnList = "category"),
        @Index(name = "idx_test_master_active", columnList = "active")
    }
)
public class TestMaster extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "test_code", nullable = false, unique = true, length = 50)
    private String testCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "test_name", nullable = false, length = 255)
    private String testName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private TestCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false, length = 50)
    private SampleType sampleType;

    @NotNull
    @Column(name = "normal_tat_minutes", nullable = false)
    private Integer normalTATMinutes; // Normal TAT in minutes

    @NotNull
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false, length = 20)
    private PriorityLevel priorityLevel = PriorityLevel.ROUTINE;

    @Column(name = "is_panel", nullable = false)
    private Boolean isPanel = false;

    @Size(max = 1000)
    @Column(name = "panel_test_codes", length = 1000)
    private String panelTestCodes; // Comma-separated test codes if this is a panel

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 1000)
    @Column(name = "normal_range", length = 1000)
    private String normalRange;

    @Size(max = 500)
    @Column(name = "instructions", length = 500)
    private String instructions;

    @Size(max = 255)
    @Column(name = "created_by_user", length = 255)
    private String createdByUser;

    public TestMaster() {
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
}
