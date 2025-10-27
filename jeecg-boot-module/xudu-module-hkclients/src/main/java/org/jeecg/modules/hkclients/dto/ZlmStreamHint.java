package org.jeecg.modules.hkclients.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZlmStreamHint {
    private String app;
    private String stream;
    private String schema;
    private String rtspUrl;
    private Integer channelNo;
    private Integer streamType;
    private String channelName;
    private String manufacturer;
    private String model;
    private String deviceIp;
    private String codecVideo;
    private String codecAudio;
    private String resolution;
}
