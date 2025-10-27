package org.jeecg.modules.hkclients.exception;

public class HKClientException extends RuntimeException {
    private int httpStatus = 0;
    public HKClientException(String message) { super(message); }
    public HKClientException(String message, Throwable cause) { super(message, cause); }
    public HKClientException(int httpStatus, String message) { super(message); this.httpStatus = httpStatus; }
    public int getHttpStatus() { return httpStatus; }
}
