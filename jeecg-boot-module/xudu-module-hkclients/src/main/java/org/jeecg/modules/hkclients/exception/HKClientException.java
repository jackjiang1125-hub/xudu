package org.jeecg.modules.hkclients.exception;

public class HKClientException extends RuntimeException {

    private final int httpStatus;
    private final HkErrorResponse hkError;

    public HKClientException(int httpStatus, HkErrorResponse hkError, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.hkError = hkError;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public HkErrorResponse getHkError() {
        return hkError;
    }


}

