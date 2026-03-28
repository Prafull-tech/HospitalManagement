package com.hospital.hms.common.exception;

import com.hospital.hms.common.logging.MdcKeys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.env.Environment;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.hospital.hms.pharmacy.exception.DuplicateMedicineCodeException;
import com.hospital.hms.pharmacy.exception.DuplicateRackCodeException;
import com.hospital.hms.pharmacy.exception.InsufficientStockException;
import com.hospital.hms.lab.exception.DuplicateTestCodeException;

/**
 * Global exception handling for REST APIs. Returns consistent JSON error bodies.
 * Logs with correlationId and userId (from MDC); logs request method and URI.
 * Stack trace only in logs, never in response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    private static void logError(HttpServletRequest request, String message, Exception ex) {
        MDC.put(MdcKeys.MODULE, "SYSTEM");
        try {
            String method = request != null ? request.getMethod() : "?";
            String uri = request != null ? request.getRequestURI() : "?";
            if (ex != null) {
                log.error("{} {} - {} - {}", method, uri, message, ex.getMessage(), ex);
            } else {
                log.error("{} {} - {}", method, uri, message);
            }
        } finally {
            MDC.remove(MdcKeys.MODULE);
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorBody> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String param = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Invalid value for parameter '%s': '%s'. Use a valid value or omit the parameter.", param, value);
        logError(request, "Validation failure: " + message, ex);
        ErrorBody body = new ErrorBody(HttpStatus.BAD_REQUEST.value(), message, Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HmsBusinessException.class)
    public ResponseEntity<ErrorBody> handleBusinessException(
            HmsBusinessException ex,
            HttpServletRequest request) {
        logError(request, ex.getErrorCode() + ": " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(ex.getHttpStatus().value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBody> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        logError(request, "Resource not found: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.NOT_FOUND.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorBody> handleOperationNotAllowed(
            OperationNotAllowedException ex,
            HttpServletRequest request) {
        logError(request, "Operation not allowed: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.FORBIDDEN.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(DuplicateBedAvailabilityException.class)
    public ResponseEntity<ErrorBody> handleDuplicateBedAvailability(
            DuplicateBedAvailabilityException ex,
            HttpServletRequest request) {
        logError(request, "Duplicate bed availability: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.CONFLICT.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateMedicineCodeException.class)
    public ResponseEntity<ErrorBody> handleDuplicateMedicineCode(
            DuplicateMedicineCodeException ex,
            HttpServletRequest request) {
        logError(request, "Duplicate medicine code: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.CONFLICT.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateTestCodeException.class)
    public ResponseEntity<ErrorBody> handleDuplicateTestCode(
            DuplicateTestCodeException ex,
            HttpServletRequest request) {
        logError(request, "Duplicate test code: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.CONFLICT.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateRackCodeException.class)
    public ResponseEntity<ErrorBody> handleDuplicateRackCode(
            DuplicateRackCodeException ex,
            HttpServletRequest request) {
        logError(request, "Duplicate rack code: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.CONFLICT.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorBody> handleInsufficientStock(
            InsufficientStockException ex,
            HttpServletRequest request) {
        logError(request, "Insufficient stock: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidBedCountsException.class)
    public ResponseEntity<ErrorBody> handleInvalidBedCounts(
            InvalidBedCountsException ex,
            HttpServletRequest request) {
        logError(request, "Invalid bed counts: " + ex.getMessage(), null);
        ErrorBody body = new ErrorBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        logError(request, "Bad request: " + ex.getMessage(), ex);
        ErrorBody body = new ErrorBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBody> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String msg = ex.getMessage();
        String userMsg = "Invalid request format. ";
        if (msg != null) {
            if (msg.contains("Cannot deserialize") || msg.contains("not a valid")) {
                userMsg += "Please check that all fields have valid values (e.g. locationArea: MAIN_STORE, ICU_STORE, COLD_ROOM; storageType: ROOM_TEMP, COLD_CHAIN).";
            } else if (msg.contains("Required request body")) {
                userMsg += "Request body is required.";
            } else {
                userMsg += "Please check your input and try again.";
            }
        } else {
            userMsg += "Please check your input and try again.";
        }
        logError(request, "Invalid request body: " + msg, ex);
        ErrorBody body = new ErrorBody(HttpStatus.BAD_REQUEST.value(), userMsg, Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorBody> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String msg = ex.getMessage();
        String userMsg = "A record with this value already exists. Please use a unique code or name.";
        if (msg != null && (msg.contains("unique") || msg.contains("duplicate") || msg.contains("Duplicate"))) {
            userMsg = "This code or value already exists. Please use a unique value.";
        }
        logError(request, "Data integrity violation: " + msg, ex);
        ErrorBody body = new ErrorBody(HttpStatus.CONFLICT.value(), userMsg, Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ErrorBody> handleLazyInit(
            LazyInitializationException ex,
            HttpServletRequest request) {
        logError(request, "LazyInitializationException - ensure entity associations are loaded in same transaction", ex);
        ErrorBody body = new ErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Data load error. Please try again.",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }
        logError(request, "Validation failed: " + errors, null);
        ErrorBody body = new ErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                Instant.now(),
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleGeneric(Exception ex, HttpServletRequest request) {
        logError(request, "Unhandled exception", ex);
        boolean isDev = environment != null && java.util.Arrays.stream(environment.getActiveProfiles()).anyMatch("dev"::equals);
        String userMessage = "An unexpected error occurred. Please try again or contact support.";
        String detail = null;
        if (isDev && ex != null) {
            String msg = ex.getMessage();
            String cls = ex.getClass().getSimpleName();
            detail = (msg != null && !msg.isBlank()) ? cls + ": " + msg : cls;
        }
        ErrorBody body = new ErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                userMessage,
                Instant.now(),
                detail
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public static class ErrorBody {
        private final int status;
        private final String message;
        private final Instant timestamp;
        private Map<String, String> errors;
        private final String detail;

        public ErrorBody(int status, String message, Instant timestamp) {
            this(status, message, timestamp, (Map<String, String>) null);
        }

        public ErrorBody(int status, String message, Instant timestamp, String detail) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.errors = null;
            this.detail = detail;
        }

        public ErrorBody(int status, String message, Instant timestamp, Map<String, String> errors) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.errors = errors;
            this.detail = null;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        /** In dev profile only: exception class and message for debugging. */
        public String getDetail() {
            return detail;
        }
    }
}
