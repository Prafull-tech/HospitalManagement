package com.hospital.hms.common.logging;

/**
 * Standardized MDC keys for request-scoped and auth-scoped logging.
 * Use these keys everywhere for correlation ID, user ID, and module.
 */
public final class MdcKeys {

    /** Unique per HTTP request; from header X-Correlation-Id or generated UUID. */
    public static final String CORRELATION_ID = "correlationId";

    /** Authenticated user identifier or ANONYMOUS; never log sensitive user details. */
    public static final String USER_ID = "userId";

    /** Module name: RECEPTION, OPD, IPD, NURSING, ICU, BILLING, SYSTEM. */
    public static final String MODULE = "module";

    /** HTTP header for client-provided correlation ID. */
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    /** Value when no authenticated user (anonymous request). */
    public static final String ANONYMOUS_USER = "ANONYMOUS";

    private MdcKeys() {
    }
}
