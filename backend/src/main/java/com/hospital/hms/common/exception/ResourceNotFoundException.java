package com.hospital.hms.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends HmsBusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "HMS_NOT_FOUND";
    }
}
