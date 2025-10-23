package com.xudu.center.zlm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zlm")
public class ZlmProperties {
    private String baseUrl;
    private String secret;

    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 5000;
    private int maxTotal = 200;
    private int defaultMaxPerRoute = 50;

    private String publicHttpBase; // 播放 URL 用
    private String publicRtspHost; // 例：127.0.0.1:554

    private Internal internal = new Internal();
    private FfmpegKeys ffmpegKeys = new FfmpegKeys();

    // 连接池/超时略…

    @Data
    public static class Internal {
        /** 例：rtmp://127.0.0.1（不带尾斜杠） */
        private String rtmpBase = "rtmp://127.0.0.1";
        /** 例：rtsp://127.0.0.1:554（不带尾斜杠） */
        private String rtspBase = "rtsp://127.0.0.1:554";
    }

    @Data
    public static class FfmpegKeys {
        private String copyRtmp;
        private String aacOnlyRtmp;
        private String h264OnlyRtmp;
        private String h264OnlyRtmpNvenc;
        private String h264AacRtmp;
        private String h264AacRtmpNvenc;
    }
}
