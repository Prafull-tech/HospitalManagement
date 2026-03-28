package com.hospital.hms.common.exception;

import org.springframework.http.HttpStatus;

public class OperationNotPermittedException extends HmsBusinessException {

    public OperationNotPermittedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return "HMS_FORBIDDEN";
    }
}
