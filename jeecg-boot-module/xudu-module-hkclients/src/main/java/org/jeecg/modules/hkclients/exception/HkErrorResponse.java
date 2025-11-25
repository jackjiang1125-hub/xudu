package org.jeecg.modules.hkclients.exception;

import lombok.Data;

@Data
public class HkErrorResponse {

    private Integer statusCode;
    private String statusString;
    private String subStatusCode;
    private Long errorCode;
    private String errorMsg;
}
