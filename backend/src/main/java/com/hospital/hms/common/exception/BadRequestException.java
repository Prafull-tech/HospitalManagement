package com.hospital.hms.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends HmsBusinessException {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "HMS_BAD_REQUEST";
    }
}
