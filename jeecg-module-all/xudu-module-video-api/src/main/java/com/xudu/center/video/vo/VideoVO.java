package com.xudu.center.video.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VideoVO {

    private String id;
    private String name;

    private String username;
    private String password;

    private String ip;
    private String port;

    private String manufacturer;
    private String model = "xudu_model_video";
    private String type; // nvr ipc

    private String app = "xudu"; // zlm 用的应用名

    private String status; // 在线状态

    private String rtspUrl; // rtsp地址
    private String hlsUrl; // 给小程序播放的地址
    private String webRtcUrl; // 给web端播放的地址

    private String stream; // 流名称

    private String ffmpegCmdKey; // 转码模板

    /** 源的编解码（便于前端展示） */
    private String videoCodec;
    private String audioCodec;

    private String parentId;
    private Integer channelNo;
    private String serialNumber;
    private String deviceId;
    private String firmwareVersion;
    private String macAddress;

    private List<VideoVO> children = new ArrayList<>();
    private List<StreamVO> streams = new ArrayList<>();
}
