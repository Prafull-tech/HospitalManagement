package com.hospital.hms.token.dto;

/**
 * Current/next token for waiting room display.
 */
public class TokenDisplayDto {

    private String currentToken;
    private String nextToken;
    private String doctorName;
    private String roomNo;

    public TokenDisplayDto() {
    }

    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }
    public String getNextToken() { return nextToken; }
    public void setNextToken(String nextToken) { this.nextToken = nextToken; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
}
