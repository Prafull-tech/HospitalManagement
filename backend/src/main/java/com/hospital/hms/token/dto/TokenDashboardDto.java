package com.hospital.hms.token.dto;

import java.util.List;

/**
 * Dashboard: waiting, in consultation, completed.
 */
public class TokenDashboardDto {

    private List<TokenResponseDto> waiting;
    private List<TokenResponseDto> inConsultation;
    private List<TokenResponseDto> completed;

    public TokenDashboardDto() {
    }

    public List<TokenResponseDto> getWaiting() { return waiting; }
    public void setWaiting(List<TokenResponseDto> waiting) { this.waiting = waiting; }
    public List<TokenResponseDto> getInConsultation() { return inConsultation; }
    public void setInConsultation(List<TokenResponseDto> inConsultation) { this.inConsultation = inConsultation; }
    public List<TokenResponseDto> getCompleted() { return completed; }
    public void setCompleted(List<TokenResponseDto> completed) { this.completed = completed; }
}
