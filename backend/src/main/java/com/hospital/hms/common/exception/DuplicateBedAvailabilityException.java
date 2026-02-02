package com.hospital.hms.common.exception;

/**
 * Thrown when creating or updating bed availability would result in a duplicate Hospital + WardType.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateBedAvailabilityException extends RuntimeException {

    public DuplicateBedAvailabilityException(String message) {
        super(message);
    }

    public DuplicateBedAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
