package com.hospital.hms.lab.exception;

/**
 * Exception thrown when attempting to create a test master with a duplicate test code.
 */
public class DuplicateTestCodeException extends RuntimeException {

    public DuplicateTestCodeException(String message) {
        super(message);
    }
}
