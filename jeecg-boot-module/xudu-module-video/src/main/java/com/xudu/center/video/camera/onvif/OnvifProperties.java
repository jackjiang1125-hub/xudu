package com.xudu.center.video.camera.onvif;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ipc.onvif")
public class OnvifProperties {

    /**
     * ONVIF REST 服务的完整 URL。
     */
    private String discoveryUrl = "http://localhost:8066/device/summary";
}
