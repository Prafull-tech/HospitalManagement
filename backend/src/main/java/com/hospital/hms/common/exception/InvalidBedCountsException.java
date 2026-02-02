package com.hospital.hms.common.exception;

/**
 * Thrown when bed counts fail validation (negative values or sum exceeds total beds).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidBedCountsException extends RuntimeException {

    public InvalidBedCountsException(String message) {
        super(message);
    }

    public InvalidBedCountsException(String message, Throwable cause) {
        super(message, cause);
    }
}
