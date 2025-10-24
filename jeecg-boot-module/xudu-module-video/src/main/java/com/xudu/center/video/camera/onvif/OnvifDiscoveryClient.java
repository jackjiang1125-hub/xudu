package com.xudu.center.video.camera.onvif;

import java.util.LinkedHashMap;
import java.util.Map;

import com.xudu.center.video.camera.onvif.dto.OnvifDiscoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OnvifDiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(OnvifDiscoveryClient.class);

    private final RestTemplate restTemplate;
    private final OnvifProperties properties;

    public OnvifDiscoveryClient(RestTemplate restTemplate, OnvifProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public OnvifDiscoveryResponse discover(String host, String port, String username, String password) {
        Assert.hasText(host, "host must not be empty");
        Assert.hasText(username, "username must not be empty");
        Assert.notNull(password, "password must not be null");
        String discoveryUrl = properties.getDiscoveryUrl();
        Assert.hasText(discoveryUrl, "请先配置 ipc.onvif.discovery-url");

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("host", host);
        payload.put("port", port);
        payload.put("user", username);
        payload.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        try {
            log.debug("Invoking ONVIF discovery at {} for host {}", discoveryUrl, host);
            ResponseEntity<OnvifDiscoveryResponse> response = restTemplate.postForEntity(discoveryUrl, entity, OnvifDiscoveryResponse.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("ONVIF 服务调用失败, 状态码: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (RestClientException ex) {
            throw new IllegalStateException("调用 ONVIF 服务失败: " + ex.getMessage(), ex);
        }
    }
}
