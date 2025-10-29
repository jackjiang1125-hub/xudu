package com.xudu.center.facecrop.model;

/**
 * Face crop processing status codes.
 */
public enum FaceCropStatus {
    SUCCESS,
    SRC_NOT_FOUND,
    INVALID_FORMAT,
    SIZE_LIMIT_EXCEEDED,
    NO_FACE_DETECTED,
    EYE_VERIFY_FAILED,
    PROCESSING_ERROR
}