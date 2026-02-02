package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.EquipmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/ipd/transfers/execute.
 */
public class ExecuteTransferRequestDto {

    @NotNull(message = "Transfer recommendation ID is required")
    private Long transferRecommendationId;

    private Long nurseId;
    private Long attendantId;
    private EquipmentType equipmentUsed;

    /** COMPLETED or IN_TRANSIT. Default COMPLETED when executing. */
    @Size(max = 20)
    private String transferStatus;

    public ExecuteTransferRequestDto() {
    }

    public Long getTransferRecommendationId() {
        return transferRecommendationId;
    }

    public void setTransferRecommendationId(Long transferRecommendationId) {
        this.transferRecommendationId = transferRecommendationId;
    }

    public Long getNurseId() {
        return nurseId;
    }

    public void setNurseId(Long nurseId) {
        this.nurseId = nurseId;
    }

    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public EquipmentType getEquipmentUsed() {
        return equipmentUsed;
    }

    public void setEquipmentUsed(EquipmentType equipmentUsed) {
        this.equipmentUsed = equipmentUsed;
    }

    public String getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(String transferStatus) {
        this.transferStatus = transferStatus;
    }
}
