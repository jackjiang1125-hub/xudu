package com.xudu.center.video.camera.onvif.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnvifDiscoveryResponse {

    private String role;
    private DeviceInfo device;
    private Map<String, Object> capabilities;
    @JsonProperty("media_version")
    private Integer mediaVersion;
    @JsonProperty("channel_count")
    private Integer channelCount;
    private List<Channel> channels;
    @JsonProperty("state_hash")
    private String stateHash;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeviceInfo {
        @JsonProperty("Manufacturer")
        private String manufacturer;
        @JsonProperty("Model")
        private String model;
        @JsonProperty("FirmwareVersion")
        private String firmwareVersion;
        @JsonProperty("SerialNumber")
        private String serialNumber;
        @JsonProperty("HardwareId")
        private String hardwareId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {
        @JsonProperty("source_token")
        private String sourceToken;
        private List<ChannelProfile> profiles;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelProfile {
        private String token;
        private String name;
        private String kind;
        @JsonProperty("VideoEncoder")
        private Map<String, Object> videoEncoder;
        @JsonProperty("AudioEncoder")
        private Map<String, Object> audioEncoder;
        private String rtsp;
    }
}
