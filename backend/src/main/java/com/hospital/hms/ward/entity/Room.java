package com.hospital.hms.ward.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Room within a ward. Optional for ICU (ward can have beds directly). DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "rooms",
    indexes = {
        @Index(name = "idx_room_ward", columnList = "ward_id"),
        @Index(name = "idx_room_ward_number", columnList = "ward_id, room_number", unique = true)
    }
)
public class Room extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @NotBlank
    @Size(max = 30)
    @Column(name = "room_number", nullable = false, length = 30)
    private String roomNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Room() {
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
