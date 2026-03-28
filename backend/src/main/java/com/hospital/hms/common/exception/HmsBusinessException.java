package com.hospital.hms.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all HMS business exceptions.
 * Carries HTTP status and machine-readable error code so GlobalExceptionHandler
 * can handle all subtypes uniformly.
 */
public abstract class HmsBusinessException extends RuntimeException {

    protected HmsBusinessException(String message) {
        super(message);
    }

    protected HmsBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract HttpStatus getHttpStatus();

    public abstract String getErrorCode();
}
