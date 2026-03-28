package com.hospital.hms.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEntityException extends HmsBusinessException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "HMS_DUPLICATE";
    }
}
