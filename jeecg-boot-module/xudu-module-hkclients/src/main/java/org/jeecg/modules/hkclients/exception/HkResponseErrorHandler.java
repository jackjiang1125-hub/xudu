package org.jeecg.modules.hkclients.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HkResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    public HkResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());
        return (statusCode != null && statusCode.isError());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        HkErrorResponse hkError = null;
        try {
            hkError = objectMapper.readValue(body, HkErrorResponse.class);
        } catch (Exception ignore) {
            // 解析失败就算了，至少还有原始 body
        }

        int statusCode = response.getRawStatusCode();
        String msg;
        if (hkError != null) {
            msg = String.format("HK error [http=%d, statusCode=%s, subStatusCode=%s, errorMsg=%s]",
                    statusCode, hkError.getStatusCode(), hkError.getSubStatusCode(), hkError.getErrorMsg());
        } else {
            msg = "HK error [http=" + statusCode + "]: " + body;
        }

        throw new HKClientException(statusCode, hkError, msg, null);
    }
}
