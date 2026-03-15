package com.hospital.hms.walkin.dto;

import com.hospital.hms.token.entity.TokenPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Walk-in registration: patient (existing or new), doctor, department, visit type, priority.
 */
public class WalkInRegisterRequestDto {

    /** Existing patient ID; if null, new patient data must be provided. */
    private Long patientId;

    /** New patient: full name (required when patientId is null). */
    @Size(max = 255)
    private String fullName;

    /** New patient: gender (required when patientId is null). */
    @Size(max = 20)
    private String gender;

    /** New patient: age (required when patientId is null). */
    private Integer age;

    /** New patient: mobile. */
    @Size(max = 30)
    private String mobile;

    /** New patient: email. */
    @Size(max = 255)
    private String email;

    /** New patient: address. */
    @Size(max = 500)
    private String address;

    /** New patient: city. */
    @Size(max = 100)
    private String city;

    /** New patient: state. */
    @Size(max = 100)
    private String state;

    /** New patient: pincode. */
    @Size(max = 20)
    private String pincode;

    /** New patient: ID proof type (e.g. AADHAAR). */
    @Size(max = 50)
    private String idProofType;

    /** New patient: ID proof number (e.g. Aadhaar number). */
    @Size(max = 100)
    private String idProofNumber;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    /** NEW or FOLLOWUP. */
    private String visitType = "NEW";

    private TokenPriority priority = TokenPriority.NORMAL;

    public WalkInRegisterRequestDto() {
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    public TokenPriority getPriority() { return priority; }
    public void setPriority(TokenPriority priority) { this.priority = priority; }
}
