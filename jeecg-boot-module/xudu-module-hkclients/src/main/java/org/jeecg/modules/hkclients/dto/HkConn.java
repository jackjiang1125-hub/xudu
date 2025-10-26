package org.jeecg.modules.hkclients.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HkConn {
    private String host;
    private int port;
    private String username;
    private String password;
    private int connectTimeoutMs;
    private int readTimeoutMs;

    public String baseUrl() { return "http://" + host + ":" + port; }
}
