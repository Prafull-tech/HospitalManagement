package com.hospital.hms.walkin.dto;

import com.hospital.hms.token.dto.TokenResponseDto;

/**
 * Walk-in registration result: patient UHID, OPD visit, token.
 */
public class WalkInRegisterResponseDto {

    private String patientUhid;
    private String patientName;
    private Long opdVisitId;
    private String visitNumber;
    private TokenResponseDto token;

    public WalkInRegisterResponseDto() {
    }

    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
    public String getVisitNumber() { return visitNumber; }
    public void setVisitNumber(String visitNumber) { this.visitNumber = visitNumber; }
    public TokenResponseDto getToken() { return token; }
    public void setToken(TokenResponseDto token) { this.token = token; }
}
