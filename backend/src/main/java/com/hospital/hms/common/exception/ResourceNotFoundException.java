package com.hospital.hms.common.exception;

/**
 * Thrown when a requested resource (e.g. patient by UHID) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
