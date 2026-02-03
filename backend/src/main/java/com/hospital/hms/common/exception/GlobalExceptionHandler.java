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
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Global exception handling for REST APIs. Returns consistent JSON error bodies.
 * Logs with correlationId and userId (from MDC); logs request method and URI.
 * Stack trace only in logs, never in response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        // #region agent log
        try {
            String stackTrace = ex.getStackTrace() != null
                ? Stream.of(ex.getStackTrace()).limit(8).map(StackTraceElement::toString).collect(Collectors.joining(" | "))
                : "";
            String cls = ex.getClass().getName() != null ? ex.getClass().getName().replace("\\", "/").replace("\"", "'") : "";
            String msg = ex.getMessage() != null ? ex.getMessage().replace("\\", "\\\\").replace("\"", "'").replace("\n", " ") : "";
            String st = stackTrace.replace("\\", "\\\\").replace("\"", "'").replace("\n", " ");
            String ndjson = "{\"location\":\"GlobalExceptionHandler.handleGeneric\",\"message\":\"Unhandled exception\",\"data\":{\"exceptionClass\":\"" + cls + "\",\"exceptionMessage\":\"" + msg + "\",\"stackTrace\":\"" + st + "\"},\"timestamp\":" + System.currentTimeMillis() + ",\"sessionId\":\"debug-session\",\"hypothesisId\":\"H5\"}\n";
            Path logPath = Paths.get("p:\\genius36\\HospitalManagement\\.cursor\\debug.log");
            Files.createDirectories(logPath.getParent());
            Files.write(logPath, ndjson.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable t) { /* ignore */ }
        // #endregion
        ErrorBody body = new ErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public static class ErrorBody {
        private final int status;
        private final String message;
        private final Instant timestamp;
        private Map<String, String> errors;

        public ErrorBody(int status, String message, Instant timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }

        public ErrorBody(int status, String message, Instant timestamp, Map<String, String> errors) {
            this(status, message, timestamp);
            this.errors = errors;
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
    }
}
