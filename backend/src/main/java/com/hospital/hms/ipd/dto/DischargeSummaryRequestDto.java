package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.Size;

public class DischargeSummaryRequestDto {

    @Size(max = 2000)
    private String diagnosisSummary;

    @Size(max = 2000)
    private String treatmentSummary;

    @Size(max = 1000)
    private String procedures;

    @Size(max = 1000)
    private String advice;

    @Size(max = 1000)
    private String followUp;

    @Size(max = 1000)
    private String medicinesOnDischarge;

    public String getDiagnosisSummary() { return diagnosisSummary; }
    public void setDiagnosisSummary(String diagnosisSummary) { this.diagnosisSummary = diagnosisSummary; }
    public String getTreatmentSummary() { return treatmentSummary; }
    public void setTreatmentSummary(String treatmentSummary) { this.treatmentSummary = treatmentSummary; }
    public String getProcedures() { return procedures; }
    public void setProcedures(String procedures) { this.procedures = procedures; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public String getFollowUp() { return followUp; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }
    public String getMedicinesOnDischarge() { return medicinesOnDischarge; }
    public void setMedicinesOnDischarge(String medicinesOnDischarge) { this.medicinesOnDischarge = medicinesOnDischarge; }
}
