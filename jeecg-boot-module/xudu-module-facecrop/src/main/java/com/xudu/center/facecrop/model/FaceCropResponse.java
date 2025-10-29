package com.xudu.center.facecrop.model;

/**
 * Standard response for face crop operations.
 */
public class FaceCropResponse {
    private boolean success;
    private FaceCropStatus status;
    private String code;
    private String message;
    private String outputPath;
    private long durationMs;

    public static FaceCropResponse ok(String outputPath, long durationMs) {
        FaceCropResponse r = new FaceCropResponse();
        r.success = true;
        r.status = FaceCropStatus.SUCCESS;
        r.code = "0";
        r.message = "ok";
        r.outputPath = outputPath;
        r.durationMs = durationMs;
        return r;
    }

    public static FaceCropResponse error(FaceCropStatus status, String code, String message) {
        FaceCropResponse r = new FaceCropResponse();
        r.success = false;
        r.status = status;
        r.code = code;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public FaceCropStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getOutputPath() { return outputPath; }
    public long getDurationMs() { return durationMs; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setStatus(FaceCropStatus status) { this.status = status; }
    public void setCode(String code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}