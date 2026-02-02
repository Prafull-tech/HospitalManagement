package com.hospital.hms.common.exception;

/**
 * Thrown when the caller's role does not allow the requested operation (logical access control).
 * Maps to HTTP 403 Forbidden.
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
