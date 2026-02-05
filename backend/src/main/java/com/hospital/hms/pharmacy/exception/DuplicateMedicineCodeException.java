package com.hospital.hms.pharmacy.exception;

/**
 * Thrown when trying to create a medicine master with a duplicate code.
 */
public class DuplicateMedicineCodeException extends RuntimeException {

    public DuplicateMedicineCodeException(String message) {
        super(message);
    }
}

