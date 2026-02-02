package com.hospital.hms.reception.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient entity for Reception module. DB-agnostic (H2 & MySQL).
 * UHID is hospital-wide unique; generated in service layer.
 * Registration number is auto-generated unique identifier.
 */
@Entity
@Table(
    name = "patients",
    indexes = {
        @Index(name = "idx_patient_uhid", columnList = "uhid", unique = true),
        @Index(name = "idx_patient_reg_no", columnList = "registration_number", unique = true),
        @Index(name = "idx_patient_phone", columnList = "phone")
    }
)
public class Patient extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "uhid", nullable = false, unique = true, length = 50)
    private String uhid;

    @NotBlank
    @Size(max = 50)
    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    @NotNull
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Size(max = 50)
    @Column(name = "id_proof_type", length = 50)
    private String idProofType;

    @Size(max = 100)
    @Column(name = "id_proof_number", length = 100)
    private String idProofNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "age_years")
    private Integer ageYears;

    @Column(name = "age_months")
    private Integer ageMonths;

    @Column(name = "age_days")
    private Integer ageDays;

    @NotBlank
    @Size(max = 20)
    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "district", length = 100)
    private String district;

    @Size(max = 255)
    @Column(name = "father_husband_name", length = 255)
    private String fatherHusbandName;

    @Size(max = 50)
    @Column(name = "referred_by", length = 50)
    private String referredBy;

    @Size(max = 255)
    @Column(name = "referred_name", length = 255)
    private String referredName;

    @Size(max = 20)
    @Column(name = "referred_phone", length = 20)
    private String referredPhone;

    @Size(max = 255)
    @Column(name = "consultant_name", length = 255)
    private String consultantName;

    @Size(max = 100)
    @Column(name = "specialization", length = 100)
    private String specialization;

    @Size(max = 50)
    @Column(name = "organisation_type", length = 50)
    private String organisationType;

    @Size(max = 255)
    @Column(name = "organisation_name", length = 255)
    private String organisationName;

    @Size(max = 1000)
    @Column(name = "remarks", length = 1000)
    private String remarks;

    public Patient() {
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdProofType() {
        return idProofType;
    }

    public void setIdProofType(String idProofType) {
        this.idProofType = idProofType;
    }

    public String getIdProofNumber() {
        return idProofNumber;
    }

    public void setIdProofNumber(String idProofNumber) {
        this.idProofNumber = idProofNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(Integer ageYears) {
        this.ageYears = ageYears;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public Integer getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(Integer ageDays) {
        this.ageDays = ageDays;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getFatherHusbandName() {
        return fatherHusbandName;
    }

    public void setFatherHusbandName(String fatherHusbandName) {
        this.fatherHusbandName = fatherHusbandName;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public String getReferredName() {
        return referredName;
    }

    public void setReferredName(String referredName) {
        this.referredName = referredName;
    }

    public String getReferredPhone() {
        return referredPhone;
    }

    public void setReferredPhone(String referredPhone) {
        this.referredPhone = referredPhone;
    }

    public String getConsultantName() {
        return consultantName;
    }

    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
