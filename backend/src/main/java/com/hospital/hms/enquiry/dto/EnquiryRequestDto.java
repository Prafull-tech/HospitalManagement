package com.hospital.hms.enquiry.dto;

import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnquiryRequestDto {

    private Long patientId;
    private Long departmentId;

    @NotNull
    private EnquiryCategory category;

    private EnquiryPriority priority = EnquiryPriority.MEDIUM;

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @Size(max = 255)
    private String enquirerName;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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
}
