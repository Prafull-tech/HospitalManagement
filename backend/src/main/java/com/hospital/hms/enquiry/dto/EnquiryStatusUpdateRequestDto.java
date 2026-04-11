package com.hospital.hms.enquiry.dto;

import com.hospital.hms.enquiry.entity.EnquiryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnquiryStatusUpdateRequestDto {
    @NotNull
    private EnquiryStatus status;

    @Size(max = 1000)
    private String resolution;

    @Size(max = 1000)
    private String note;

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
