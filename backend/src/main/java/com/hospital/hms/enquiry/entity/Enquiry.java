package com.hospital.hms.enquiry.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.reception.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(
    name = "enquiries",
    indexes = {
        @Index(name = "idx_enquiry_status", columnList = "status"),
        @Index(name = "idx_enquiry_department", columnList = "department_id"),
        @Index(name = "idx_enquiry_patient", columnList = "patient_id"),
        @Index(name = "idx_enquiry_assignee", columnList = "assigned_to_user")
    }
)
public class Enquiry extends BaseIdEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "enquiry_no", nullable = false, unique = true, length = 30)
    private String enquiryNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MedicalDepartment department;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private EnquiryCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private EnquiryPriority priority = EnquiryPriority.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnquiryStatus status = EnquiryStatus.OPEN;

    @NotBlank
    @Size(max = 255)
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Size(max = 1000)
    @Column(name = "resolution", length = 1000)
    private String resolution;

    @Size(max = 255)
    @Column(name = "assigned_to_user", length = 255)
    private String assignedToUser;

    @Size(max = 255)
    @Column(name = "enquirer_name", length = 255)
    private String enquirerName;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public String getEnquiryNo() {
        return enquiryNo;
    }

    public void setEnquiryNo(String enquiryNo) {
        this.enquiryNo = enquiryNo;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public MedicalDepartment getDepartment() {
        return department;
    }

    public void setDepartment(MedicalDepartment department) {
        this.department = department;
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

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
