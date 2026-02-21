package com.hospital.hms.pharmacy.exception;

/**
 * Thrown when sell quantity exceeds available stock.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
