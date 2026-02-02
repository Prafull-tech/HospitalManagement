package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ward.entity.WardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Transfer Policy Master. Manages safe and legal patient transfers between wards.
 * <ul>
 *   <li>General → Private → ICU allowed (upgrade)</li>
 *   <li>ICU → Ward allowed (downgrade)</li>
 *   <li>External transfer flagged separately (transferType = EXTERNAL)</li>
 * </ul>
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "transfer_policy",
    indexes = {
        @Index(name = "idx_transfer_policy_type", columnList = "transfer_type"),
        @Index(name = "idx_transfer_policy_from_ward", columnList = "from_ward_type"),
        @Index(name = "idx_transfer_policy_to_ward", columnList = "to_ward_type"),
        @Index(name = "idx_transfer_policy_active", columnList = "is_active"),
        @Index(name = "uk_transfer_policy_route", columnList = "transfer_type, from_ward_type, to_ward_type", unique = true)
    }
)
public class TransferPolicy extends BaseIdEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    private TransferType transferType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_ward_type", nullable = false, length = 30)
    private WardType fromWardType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_ward_type", nullable = false, length = 30)
    private WardType toWardType;

    /** True if transfer is an upgrade (e.g. General → ICU). */
    @Column(name = "upgrade", nullable = false)
    private Boolean upgrade = false;

    /** True if transfer is a downgrade (e.g. ICU → Ward). */
    @Column(name = "downgrade", nullable = false)
    private Boolean downgrade = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public TransferPolicy() {
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
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

    public Boolean getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(Boolean upgrade) {
        this.upgrade = upgrade;
    }

    public Boolean getDowngrade() {
        return downgrade;
    }

    public void setDowngrade(Boolean downgrade) {
        this.downgrade = downgrade;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
