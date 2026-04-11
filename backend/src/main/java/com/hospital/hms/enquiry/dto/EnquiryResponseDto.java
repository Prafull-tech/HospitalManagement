package com.hospital.hms.enquiry.dto;

import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import com.hospital.hms.enquiry.entity.EnquiryStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EnquiryResponseDto {
    private Long id;
    private String enquiryNo;
    private Long patientId;
    private String patientUhid;
    private String patientName;
    private Long departmentId;
    private String departmentName;
    private EnquiryCategory category;
    private EnquiryPriority priority;
    private EnquiryStatus status;
    private String subject;
    private String description;
    private String resolution;
    private String assignedToUser;
    private String enquirerName;
    private String phone;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private List<EnquiryAuditLogDto> auditLogs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnquiryNo() {
        return enquiryNo;
    }

    public void setEnquiryNo(String enquiryNo) {
        this.enquiryNo = enquiryNo;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public EnquiryCategory getCategory() {
        return category;
    }

    public void setCategory(EnquiryCategory category) {
        this.category = category;
    }

    public EnquiryPriority getPriority() {
        return priority;
    }

    public void setPriority(EnquiryPriority priority) {
        this.priority = priority;
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(String assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public String getEnquirerName() {
        return enquirerName;
    }

    public void setEnquirerName(String enquirerName) {
        this.enquirerName = enquirerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<EnquiryAuditLogDto> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<EnquiryAuditLogDto> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
