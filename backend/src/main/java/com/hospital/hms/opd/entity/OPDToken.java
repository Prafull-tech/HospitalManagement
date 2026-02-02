package com.hospital.hms.opd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Token for queue: one per visit, token number resets per doctor per day. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "opd_tokens",
    indexes = {
        @Index(name = "idx_opd_token_doctor_date", columnList = "doctor_id, token_date"),
        @Index(name = "idx_opd_token_doctor_date_num", columnList = "doctor_id, token_date, token_number", unique = true)
    }
)
public class OPDToken extends BaseIdEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    private OPDVisit visit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Column(name = "token_date", nullable = false)
    private LocalDate tokenDate;

    @NotNull
    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    public OPDToken() {
    }

    public OPDVisit getVisit() {
        return visit;
    }

    public void setVisit(OPDVisit visit) {
        this.visit = visit;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(LocalDate tokenDate) {
        this.tokenDate = tokenDate;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(Integer tokenNumber) {
        this.tokenNumber = tokenNumber;
    }
}
