package org.jeecg.modules.hkclients.model.streaming;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * 单个 StreamingChannel 元素（ISAPI 命名空间）
 * 注意：很多字段在不同固件上名字略有差异，这里同时兼容，例如：
 * - 视频编码：videoCodecType / codecType（二选一）
 * - 码率：constantBitRate / fixedBitRate（二选一）
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingChannel {
    public static final String ISAPI_NS = "http://www.isapi.org/ver20/XMLSchema";

    // ---------- 顶层 ----------
    @XmlElement(name = "id",          namespace = ISAPI_NS) private String  id;          // 101/102/201/202...
    @XmlElement(name = "channelName", namespace = ISAPI_NS) private String  channelName;
    @XmlElement(name = "enabled",     namespace = ISAPI_NS) private Boolean enabled;

    @XmlElement(name = "Transport",   namespace = ISAPI_NS) private Transport transport;
    @XmlElement(name = "Video",       namespace = ISAPI_NS) private Video     video;
    @XmlElement(name = "Audio",       namespace = ISAPI_NS) private Audio     audio;

    // ---------- Transport ----------
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class Transport {
        @XmlElement(name = "ControlProtocolList", namespace = ISAPI_NS)
        private ControlProtocolList controlProtocolList;
    }
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class ControlProtocolList {
        @XmlElement(name = "ControlProtocol", namespace = ISAPI_NS)
        private List<ControlProtocol> controlProtocol;
    }
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class ControlProtocol {
        @XmlElement(name = "streamingTransport", namespace = ISAPI_NS)
        private String streamingTransport; // RTSP
    }

    // ---------- Video ----------
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class Video {
        @XmlElement(name = "enabled",                 namespace = ISAPI_NS) private Boolean enabled;
        @XmlElement(name = "dynVideoInputChannelID",  namespace = ISAPI_NS) private Integer dynVideoInputChannelID;

        // 编码，两种命名其一：videoCodecType / codecType
        @XmlElement(name = "videoCodecType",          namespace = ISAPI_NS) private String  videoCodecType;
        @XmlElement(name = "codecType",               namespace = ISAPI_NS) private String  codecType;

        @XmlElement(name = "videoResolutionWidth",    namespace = ISAPI_NS) private Integer videoResolutionWidth;
        @XmlElement(name = "videoResolutionHeight",   namespace = ISAPI_NS) private Integer videoResolutionHeight;

        // 码控类型常见为 CBR/VBR（这里名字就是 videoQualityControlType）
        @XmlElement(name = "videoQualityControlType", namespace = ISAPI_NS) private String  videoQualityControlType;

        // 码率，两种命名其一：constantBitRate / fixedBitRate
        @XmlElement(name = "constantBitRate",         namespace = ISAPI_NS) private Integer constantBitRate;
        @XmlElement(name = "fixedBitRate",            namespace = ISAPI_NS) private Integer fixedBitRate;

        @XmlElement(name = "maxFrameRate",            namespace = ISAPI_NS) private Integer maxFrameRate;
        @XmlElement(name = "GovLength",               namespace = ISAPI_NS) private Integer govLength;
        @XmlElement(name = "profile",                 namespace = ISAPI_NS) private String  profile;
        @XmlElement(name = "snapShotImageType",       namespace = ISAPI_NS) private String  snapShotImageType;

        @XmlElement(name = "SmartCodec",              namespace = ISAPI_NS) private SmartCodec smartCodec;
        @XmlElement(name = "SVC",                     namespace = ISAPI_NS) private SVC       svc;

    }
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class SmartCodec {
        @XmlElement(name = "enabled", namespace = ISAPI_NS) private Boolean enabled;
    }
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class SVC {
        @XmlElement(name = "enabled", namespace = ISAPI_NS) private Boolean enabled;
        @XmlElement(name = "SVCMode", namespace = ISAPI_NS) private String  svcMode;
    }

    // ---------- Audio ----------
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class Audio {
        @XmlElement(name = "enabled",              namespace = ISAPI_NS) private Boolean enabled;
        @XmlElement(name = "audioInputChannelID",  namespace = ISAPI_NS) private Integer audioInputChannelID;
        @XmlElement(name = "audioCompressionType", namespace = ISAPI_NS) private String  audioCompressionType;

        // 兼容其他固件（可选）
        @XmlElement(name = "codecType",            namespace = ISAPI_NS) private String  codecType;
        @XmlElement(name = "samplingRate",         namespace = ISAPI_NS) private Integer samplingRate;
        @XmlElement(name = "bitRate",              namespace = ISAPI_NS) private Integer bitRate;
        @XmlElement(name = "channels",             namespace = ISAPI_NS) private Integer channels;
    }
}
