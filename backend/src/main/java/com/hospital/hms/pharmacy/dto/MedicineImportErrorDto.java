package com.hospital.hms.pharmacy.dto;

/**
 * Represents a single row validation error during medicine import.
 */
public class MedicineImportErrorDto {

    private int row;
    private String error;

    public MedicineImportErrorDto() {
    }

    public MedicineImportErrorDto(int row, String error) {
        this.row = row;
        this.error = error;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
