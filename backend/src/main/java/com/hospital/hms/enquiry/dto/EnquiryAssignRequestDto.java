package com.hospital.hms.enquiry.dto;

import jakarta.validation.constraints.Size;

public class EnquiryAssignRequestDto {
    private Long departmentId;

    @Size(max = 255)
    private String assignedToUser;

    @Size(max = 1000)
    private String note;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(String assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
