package org.jeecg.modules.hkclients.model.streaming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * Jackson-XML 版 StreamingChannelList
 * 适配：/ISAPI/Streaming/channels
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "StreamingChannelList")
public class StreamingChannelList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "StreamingChannel")
    private List<StreamingChannel> channels;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreamingChannel {
        @JacksonXmlProperty(localName = "id")
        private String id; // 101/102/201/202 ... 有些固件是字符串

        @JacksonXmlProperty(localName = "channelName")
        private String channelName;

        @JacksonXmlProperty(localName = "enabled")
        private Boolean enabled;

        @JacksonXmlProperty(localName = "Transport")
        private Transport transport;

        @JacksonXmlProperty(localName = "Video")
        private Video video;

        @JacksonXmlProperty(localName = "Audio")
        private Audio audio;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transport {
        @JacksonXmlProperty(localName = "ControlProtocolList")
        private ControlProtocolList controlProtocolList;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ControlProtocolList {
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "ControlProtocol")
            private List<ControlProtocol> items;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ControlProtocol {
            @JacksonXmlProperty(localName = "streamingTransport")
            private String streamingTransport; // RTSP / HTTP
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Video {
        @JacksonXmlProperty(localName = "enabled")
        private Boolean enabled;

        @JacksonXmlProperty(localName = "dynVideoInputChannelID")
        private Integer dynVideoInputChannelID;

        @JacksonXmlProperty(localName = "videoCodecType")
        private String videoCodecType; // H.264 / H.265

        @JacksonXmlProperty(localName = "codecType")
        private String codecType; // 兼容字段

        public String getCodecTypeEffective() {
            return (videoCodecType != null && !videoCodecType.isEmpty()) ? videoCodecType : codecType;
        }

        @JacksonXmlProperty(localName = "videoResolutionWidth")
        private Integer videoResolutionWidth;

        @JacksonXmlProperty(localName = "videoResolutionHeight")
        private Integer videoResolutionHeight;

        @JacksonXmlProperty(localName = "videoQualityControlType")
        private String videoQualityControlType; // CBR / VBR

        @JacksonXmlProperty(localName = "constantBitRate")
        private Integer constantBitRate;

        @JacksonXmlProperty(localName = "fixedBitRate")
        private Integer fixedBitRate;

        public Integer getBitRateEffective() {
            return constantBitRate != null ? constantBitRate : fixedBitRate;
        }

        @JacksonXmlProperty(localName = "maxFrameRate")
        private Integer maxFrameRate;

        @JacksonXmlProperty(localName = "GovLength")
        private Integer govLength;

        @JacksonXmlProperty(localName = "profile")
        private String profile;

        @JacksonXmlProperty(localName = "snapShotImageType")
        private String snapShotImageType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audio {
        @JacksonXmlProperty(localName = "enabled")
        private Boolean enabled;

        @JacksonXmlProperty(localName = "audioInputChannelID")
        private Integer audioInputChannelID;

        @JacksonXmlProperty(localName = "audioCompressionType")
        private String audioCompressionType; // G.711ulaw 等

        @JacksonXmlProperty(localName = "codecType")
        private String codecType; // 兼容字段
    }
}