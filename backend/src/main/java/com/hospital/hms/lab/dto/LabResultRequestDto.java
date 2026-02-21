package com.hospital.hms.lab.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for entering lab results (can contain multiple parameters).
 */
public class LabResultRequestDto {

    @NotNull(message = "Test order ID is required")
    private Long testOrderId;

    private List<ResultParameterDto> parameters;

    @Size(max = 1000)
    private String remarks;

    public LabResultRequestDto() {
    }

    public Long getTestOrderId() {
        return testOrderId;
    }

    public void setTestOrderId(Long testOrderId) {
        this.testOrderId = testOrderId;
    }

    public List<ResultParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(List<ResultParameterDto> parameters) {
        this.parameters = parameters;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Inner DTO for a single result parameter.
     */
    public static class ResultParameterDto {
        @Size(max = 255)
        private String parameterName;

        @Size(max = 1000)
        private String resultValue;

        @Size(max = 100)
        private String unit;

        @Size(max = 1000)
        private String normalRange;

        @Size(max = 50)
        private String flag; // H, L, N, CRITICAL

        private Boolean isCritical = false;

        public ResultParameterDto() {
        }

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getResultValue() {
            return resultValue;
        }

        public void setResultValue(String resultValue) {
            this.resultValue = resultValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getNormalRange() {
            return normalRange;
        }

        public void setNormalRange(String normalRange) {
            this.normalRange = normalRange;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public Boolean getIsCritical() {
            return isCritical;
        }

        public void setIsCritical(Boolean isCritical) {
            this.isCritical = isCritical;
        }
    }
}
