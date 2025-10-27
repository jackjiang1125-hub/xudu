package org.jeecg.modules.hkclients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NvrDeviceOverview {
    private String deviceName;
    private String deviceId;
    private String model;
    private String firmwareVersion;
    private String macAddress;
    private String serialNumber;
    private String ipv4Address;
    private Integer channelCount;
    private List<NvrChannel> channels;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NvrChannel {
        private Integer id;
        private String name;
        private String ipAddress;
        private String manufacturer;
        private String model;
        private Boolean online;
        private String rtspMain;
        private String rtspSub;
        private String rtspThird;
        private String userName;
        private String serialNumber;

        @Builder.Default
        private List<StreamInfo> streams = new ArrayList<>();

        @Data @Builder @NoArgsConstructor @AllArgsConstructor
        public static class StreamInfo {
            private Integer trackId;
            private String  rtsp;
            private String  videoCodec;
            private Integer width;
            private Integer height;
            private Integer frameRate;
            private String  bitRateType;
            private Integer bitRate;
            private String  profile;
            private Integer gop;
            private Boolean audioEnabled;
            private String  audioCodec;
            private Integer audioSampleRate;
            private Integer audioChannels;
            private Integer audioBitRate;
        }
    }
}
