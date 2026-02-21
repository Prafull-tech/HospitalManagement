package com.hospital.hms.pharmacy.exception;

/**
 * Thrown when trying to create a rack with a duplicate code.
 */
public class DuplicateRackCodeException extends RuntimeException {

    public DuplicateRackCodeException(String message) {
        super(message);
    }
}
