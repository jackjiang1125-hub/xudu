package com.xudu.center.video.vo;

import lombok.Data;

@Data
public class StreamVO {
    private String id;
    private String videoId;
    private Integer streamNo;
    private Integer trackId;
    private String streamName;
    private String rtspUrl;
    private String videoCodec;
    private Integer width;
    private Integer height;
    private Integer frameRate;
    private String bitRateType;
    private Integer bitRate;
    private String profile;
    private Integer gop;
    private Boolean audioEnabled;
    private String audioCodec;
    private Integer audioSampleRate;
    private Integer audioChannels;
    private Integer audioBitRate;
}
