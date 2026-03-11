package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.LabOrderItemSampleStatus;
import com.hospital.hms.lab.entity.LabOrderItemStatus;
import com.hospital.hms.lab.entity.TestStatus;

/**
 * Response DTO for lab order item.
 */
public class LabOrderItemResponseDto {

    private Long id;
    private Long orderId;
    private Long testId;
    private String testCode;
    private String testName;
    private LabOrderItemStatus status;
    private LabOrderItemSampleStatus sampleStatus;
    private Long testOrderId; // Link to TestOrder for lab workflow

    // Display fields for sample processing list (populated when listing pending items)
    private String orderNumber;
    private String patientUhid;
    private String patientName;
    private String sampleCollectedAt;
    private Boolean isPriority;
    private TestStatus testOrderStatus; // COLLECTED -> Start Processing, IN_PROGRESS -> Mark Processed
    private String resultEnteredAt;
    private String resultEnteredBy;

    public LabOrderItemResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
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

    public LabOrderItemStatus getStatus() {
        return status;
    }

    public void setStatus(LabOrderItemStatus status) {
        this.status = status;
    }

    public LabOrderItemSampleStatus getSampleStatus() {
        return sampleStatus;
    }

    public void setSampleStatus(LabOrderItemSampleStatus sampleStatus) {
        this.sampleStatus = sampleStatus;
    }

    public Long getTestOrderId() {
        return testOrderId;
    }

    public void setTestOrderId(Long testOrderId) {
        this.testOrderId = testOrderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getSampleCollectedAt() {
        return sampleCollectedAt;
    }

    public void setSampleCollectedAt(String sampleCollectedAt) {
        this.sampleCollectedAt = sampleCollectedAt;
    }

    public Boolean getIsPriority() {
        return isPriority;
    }

    public void setIsPriority(Boolean isPriority) {
        this.isPriority = isPriority;
    }

    public TestStatus getTestOrderStatus() {
        return testOrderStatus;
    }

    public void setTestOrderStatus(TestStatus testOrderStatus) {
        this.testOrderStatus = testOrderStatus;
    }

    public String getResultEnteredAt() {
        return resultEnteredAt;
    }

    public void setResultEnteredAt(String resultEnteredAt) {
        this.resultEnteredAt = resultEnteredAt;
    }

    public String getResultEnteredBy() {
        return resultEnteredBy;
    }

    public void setResultEnteredBy(String resultEnteredBy) {
        this.resultEnteredBy = resultEnteredBy;
    }
}
