package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.ward.entity.WardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Doctor recommendation record for patient transfers.
 * <ul>
 *   <li>Written order mandatory (except when emergency flag is set)</li>
 *   <li>Emergency allows verbal flag; record still created for audit</li>
 * </ul>
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "transfer_recommendation",
    indexes = {
        @Index(name = "idx_transfer_rec_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_transfer_rec_doctor", columnList = "recommended_by_doctor_id"),
        @Index(name = "idx_transfer_rec_time", columnList = "recommendation_time"),
        @Index(name = "idx_transfer_rec_emergency", columnList = "emergency_flag")
    }
)
public class TransferRecommendation extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_by_doctor_id", nullable = false)
    private Doctor recommendedByDoctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_ward_type", nullable = false, length = 30)
    private WardType fromWardType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_ward_type", nullable = false, length = 30)
    private WardType toWardType;

    /** Reference to indication/master (e.g. transfer indication code). Nullable if not applicable. */
    @Column(name = "indication_id")
    private Long indicationId;

    @Size(max = 1000)
    @Column(name = "recommendation_notes", length = 1000)
    private String recommendationNotes;

    /** True when verbal/emergency order; written order not mandatory. Consent may be bypassed temporarily. */
    @Column(name = "emergency_flag", nullable = false)
    private Boolean emergencyFlag = false;

    @NotNull
    @Column(name = "recommendation_time", nullable = false)
    private Instant recommendationTime;

    /** Written justification required later for emergency transfers. Prevents misuse. */
    @Size(max = 2000)
    @Column(name = "emergency_justification", length = 2000)
    private String emergencyJustification;

    @Column(name = "emergency_justification_at")
    private Instant emergencyJustificationAt;

    @Size(max = 255)
    @Column(name = "emergency_justification_by", length = 255)
    private String emergencyJustificationBy;

    public TransferRecommendation() {
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public Doctor getRecommendedByDoctor() {
        return recommendedByDoctor;
    }

    public void setRecommendedByDoctor(Doctor recommendedByDoctor) {
        this.recommendedByDoctor = recommendedByDoctor;
    }

    public WardType getFromWardType() {
        return fromWardType;
    }

    public void setFromWardType(WardType fromWardType) {
        this.fromWardType = fromWardType;
    }

    public WardType getToWardType() {
        return toWardType;
    }

    public void setToWardType(WardType toWardType) {
        this.toWardType = toWardType;
    }

    public Long getIndicationId() {
        return indicationId;
    }

    public void setIndicationId(Long indicationId) {
        this.indicationId = indicationId;
    }

    public String getRecommendationNotes() {
        return recommendationNotes;
    }

    public void setRecommendationNotes(String recommendationNotes) {
        this.recommendationNotes = recommendationNotes;
    }

    public Boolean getEmergencyFlag() {
        return emergencyFlag;
    }

    public void setEmergencyFlag(Boolean emergencyFlag) {
        this.emergencyFlag = emergencyFlag;
    }

    public Instant getRecommendationTime() {
        return recommendationTime;
    }

    public void setRecommendationTime(Instant recommendationTime) {
        this.recommendationTime = recommendationTime;
    }

    public String getEmergencyJustification() {
        return emergencyJustification;
    }

    public void setEmergencyJustification(String emergencyJustification) {
        this.emergencyJustification = emergencyJustification;
    }

    public Instant getEmergencyJustificationAt() {
        return emergencyJustificationAt;
    }

    public void setEmergencyJustificationAt(Instant emergencyJustificationAt) {
        this.emergencyJustificationAt = emergencyJustificationAt;
    }

    public String getEmergencyJustificationBy() {
        return emergencyJustificationBy;
    }

    public void setEmergencyJustificationBy(String emergencyJustificationBy) {
        this.emergencyJustificationBy = emergencyJustificationBy;
    }
}
