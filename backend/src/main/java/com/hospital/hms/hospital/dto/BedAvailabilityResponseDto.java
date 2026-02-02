package com.hospital.hms.hospital.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;

/**
 * Bed availability GET response. Matches format: Ward Type | Total Beds | Occupied | Vacant | Reserved | Under Cleaning.
 * Vacant is calculated dynamically (totalBeds - occupied - reserved - underCleaning); not stored.
 * Sorted by ward type. DTO-based; clean, readable JSON.
 *
 * <p>Sample JSON response (single item):
 * <pre>
 * {
 *   "id": 1,
 *   "hospitalId": 1,
 *   "wardType": "GENERAL",
 *   "totalBeds": 20,
 *   "occupied": 12,
 *   "vacant": 5,
 *   "reserved": 1,
 *   "underCleaning": 2,
 *   "createdAt": "2026-01-31T10:00:00Z",
 *   "updatedAt": "2026-01-31T10:00:00Z"
 * }
 * </pre>
 *
 * <p>Sample JSON response (list GET /api/hospitals/1/bed-availability):
 * <pre>
 * [
 *   {
 *     "id": 1,
 *     "hospitalId": 1,
 *     "wardType": "EMERGENCY",
 *     "totalBeds": 6,
 *     "occupied": 3,
 *     "vacant": 2,
 *     "reserved": 0,
 *     "underCleaning": 1,
 *     "createdAt": "2026-01-31T10:00:00Z",
 *     "updatedAt": "2026-01-31T10:00:00Z"
 *   },
 *   {
 *     "id": 2,
 *     "hospitalId": 1,
 *     "wardType": "GENERAL",
 *     "totalBeds": 20,
 *     "occupied": 12,
 *     "vacant": 5,
 *     "reserved": 1,
 *     "underCleaning": 2,
 *     "createdAt": "2026-01-31T10:00:00Z",
 *     "updatedAt": "2026-01-31T10:00:00Z"
 *   },
 *   {
 *     "id": 3,
 *     "hospitalId": 1,
 *     "wardType": "ICU",
 *     "totalBeds": 8,
 *     "occupied": 5,
 *     "vacant": 2,
 *     "reserved": 1,
 *     "underCleaning": 0,
 *     "createdAt": "2026-01-31T10:00:00Z",
 *     "updatedAt": "2026-01-31T10:00:00Z"
 *   }
 * ]
 * </pre>
 */
@JsonPropertyOrder({
        "id",
        "hospitalId",
        "wardType",
        "totalBeds",
        "occupied",
        "vacant",
        "reserved",
        "underCleaning",
        "createdAt",
        "updatedAt",
        "updatedBy"
})
public class BedAvailabilityResponseDto {

    private Long id;
    private Long hospitalId;
    private String wardType;
    private Integer totalBeds;
    private Integer occupied;
    private Integer vacant;
    private Integer reserved;
    private Integer underCleaning;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getWardType() {
        return wardType;
    }

    public void setWardType(String wardType) {
        this.wardType = wardType;
    }

    public Integer getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(Integer totalBeds) {
        this.totalBeds = totalBeds;
    }

    /** Occupied beds (column: Occupied). */
    public Integer getOccupied() {
        return occupied;
    }

    public void setOccupied(Integer occupied) {
        this.occupied = occupied;
    }

    /** Vacant beds, calculated: totalBeds - (occupied + reserved + underCleaning). */
    public Integer getVacant() {
        return vacant;
    }

    public void setVacant(Integer vacant) {
        this.vacant = vacant;
    }

    /** Reserved beds (column: Reserved). */
    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }

    /** Under cleaning beds (column: Under Cleaning). */
    public Integer getUnderCleaning() {
        return underCleaning;
    }

    public void setUnderCleaning(Integer underCleaning) {
        this.underCleaning = underCleaning;
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

    /** User who last updated (username or user id). */
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
